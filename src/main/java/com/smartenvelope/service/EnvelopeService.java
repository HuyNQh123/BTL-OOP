package com.smartenvelope.service;

import com.smartenvelope.dto.envelope.EnvelopeRequest;
import com.smartenvelope.dto.envelope.EnvelopeResponse;
import com.smartenvelope.entity.*;
import com.smartenvelope.enums.BudgetStatus;
import com.smartenvelope.enums.CategoryType;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnvelopeService {
    private final EnvelopeRepository envelopeRepository;
    private final BudgetPeriodRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final WorkspaceContextService workspaceContext;
    private final CurrentUserService currentUserService;
    private final TransactionRepository transactionRepository;
    private final EnvelopeTransferRepository transferRepository;
    private final PiggyBankEntryRepository piggyBankEntryRepository;

    public List<EnvelopeResponse> getCurrentWorkspaceEnvelopes(Long budgetId) {
        Long workspaceId = workspaceContext.getCurrentWorkspaceId();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        return budgetRepository.findByWorkspaceIdOrderByYearDescMonthDesc(workspaceId).stream()
                .filter(b -> budgetId == null ? b.getMonth() == today.getMonthValue() && b.getYear() == today.getYear()
                        && b.getStatus() != BudgetStatus.CLOSED : b.getId().equals(budgetId))
                .flatMap(b -> envelopeRepository.findByBudgetPeriodId(b.getId()).stream())
                .map(EnvelopeResponse::new).toList();
    }

    public List<BudgetPeriod> getEditableBudgets() {
        return budgetRepository.findByWorkspaceIdOrderByYearDescMonthDesc(workspaceContext.getCurrentWorkspaceId())
                .stream().filter(b -> b.getStatus() == BudgetStatus.DRAFT).toList();
    }

    public BudgetPeriod getBudget(Long budgetId) {
        Long workspaceId = workspaceContext.getCurrentWorkspaceId();
        return budgetRepository.findById(budgetId).filter(b -> b.getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kỳ ngân sách."));
    }

    public BudgetPeriod getEditableBudget(Long budgetId) {
        BudgetPeriod budget = getBudget(budgetId);
        if (budget.getStatus() != BudgetStatus.DRAFT)
            throw new IllegalArgumentException("Chỉ có thể thêm phong bì cho kỳ nháp.");
        return budget;
    }

    public Envelope getEditableEnvelope(Long envelopeId) {
        Long workspaceId = workspaceContext.getCurrentWorkspaceId();
        Envelope envelope = envelopeRepository.findById(envelopeId)
                .filter(e -> e.getBudgetPeriod().getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phong bì."));
        if (envelope.getBudgetPeriod().getStatus() != BudgetStatus.DRAFT)
            throw new IllegalArgumentException("Chỉ có thể sửa hạn mức của phong bì thuộc kỳ nháp.");
        return envelope;
    }

    public List<Category> getExpenseCategories() {
        return categoryRepository.findAllAvailableForUserAndType(currentUserService.getCurrentUserId(), CategoryType.EXPENSE)
                .stream().filter(c -> Boolean.TRUE.equals(c.getIsActive())).toList();
    }

    @Transactional
    public Envelope createEnvelope(EnvelopeRequest request) {
        requireEditorRole();
        Long workspaceId = workspaceContext.getCurrentWorkspaceId();
        BudgetPeriod budget = budgetRepository.findById(request.budgetPeriodId())
                .filter(b -> b.getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kỳ ngân sách."));
        if (budget.getStatus() != BudgetStatus.DRAFT) throw new IllegalArgumentException("Chỉ được sửa phân bổ của kỳ nháp.");
        Category category = categoryRepository.findById(request.categoryId())
                .filter(c -> c.getType() == CategoryType.EXPENSE && Boolean.TRUE.equals(c.getIsActive()))
                .filter(c -> Boolean.TRUE.equals(c.getIsSystem()) || (c.getUser() != null && c.getUser().getId().equals(currentUserService.getCurrentUserId())))
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục chi tiêu không hợp lệ."));
        String envelopeName = isOtherCategory(category) ? requireCustomEnvelopeName(request.name()) : category.getName();
        if (isOtherCategory(category)) {
            Long userId = currentUserService.getCurrentUserId();
            category = categoryRepository.findAllAvailableForUserAndType(userId, CategoryType.EXPENSE).stream()
                    .filter(existing -> existing.getType() == CategoryType.EXPENSE
                            && Boolean.TRUE.equals(existing.getIsActive())
                            && existing.getName().equalsIgnoreCase(envelopeName))
                    .findFirst()
                    .orElseGet(() -> categoryRepository.save(new Category(envelopeName, CategoryType.EXPENSE,
                            "bi-tag", "#607D8B", currentUserService.getCurrentUser(), false)));
        }
        BigDecimal amount = request.allocatedAmount();
        if (amount.stripTrailingZeros().scale() > 0) throw new IllegalArgumentException("Số tiền phải là số nguyên đồng Việt Nam.");
        if (envelopeRepository.findByBudgetPeriodIdAndCategoryId(budget.getId(), category.getId()).isPresent())
            throw new IllegalArgumentException("Danh mục này đã có phong bì trong kỳ ngân sách.");
        BigDecimal assigned = envelopeRepository.findByBudgetPeriodId(budget.getId()).stream()
                .map(Envelope::getAllocatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (assigned.add(amount).compareTo(budget.getTotalIncome()) > 0)
            throw new IllegalArgumentException("Tổng phân bổ không được vượt quá tiền khả dụng của kỳ.");
        Envelope envelope = envelopeRepository.save(new Envelope(budget, category, envelopeName, amount, 80));
        budget.setTotalAllocated(assigned.add(amount));
        return envelope;
    }

    @Transactional
    public Long deleteDraftEnvelope(Long envelopeId) {
        requireEditorRole();
        Long workspaceId = workspaceContext.getCurrentWorkspaceId();
        Envelope envelope = envelopeRepository.findById(envelopeId)
                .filter(e -> e.getBudgetPeriod().getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phong bì trong quỹ hiện tại."));
        BudgetPeriod budget = envelope.getBudgetPeriod();
        if (budget.getStatus() != BudgetStatus.DRAFT)
            throw new IllegalArgumentException("Chỉ có thể xóa phong bì của kỳ ngân sách nháp.");
        if (transactionRepository.existsByEnvelopeId(envelopeId)
                || piggyBankEntryRepository.existsBySourceEnvelopeId(envelopeId)
                || !transferRepository.findByFromEnvelopeId(envelopeId).isEmpty()
                || !transferRepository.findByToEnvelopeId(envelopeId).isEmpty())
            throw new IllegalArgumentException("Không thể xóa phong bì đã có giao dịch hoặc lịch sử điều chuyển.");
        budget.setTotalAllocated(budget.getTotalAllocated().subtract(envelope.getAllocatedAmount()));
        envelopeRepository.delete(envelope);
        return budget.getId();
    }

    @Transactional
    public Long updateDraftEnvelope(Long envelopeId, EnvelopeRequest request) {
        requireEditorRole();
        Envelope envelope = getEditableEnvelope(envelopeId);
        BudgetPeriod budget = envelope.getBudgetPeriod();
        if (!budget.getId().equals(request.budgetPeriodId())
                || !envelope.getCategory().getId().equals(request.categoryId()))
            throw new IllegalArgumentException("Không thể đổi kỳ hoặc danh mục khi sửa phong bì.");
        BigDecimal amount = request.allocatedAmount();
        if (amount.stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("Số tiền phải là số nguyên đồng Việt Nam.");
        String envelopeName = requireCustomEnvelopeName(request.name());
        if (amount.compareTo(envelope.getSpentAmount()) < 0)
            throw new IllegalArgumentException("Hạn mức mới không thể thấp hơn số tiền đã chi ("
                    + envelope.getSpentAmount().toPlainString() + " ₫).");
        BigDecimal currentTotal = envelopeRepository.findByBudgetPeriodId(budget.getId()).stream()
                .map(Envelope::getAllocatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal updatedTotal = currentTotal.subtract(envelope.getAllocatedAmount()).add(amount);
        if (updatedTotal.compareTo(budget.getTotalIncome()) > 0)
            throw new IllegalArgumentException("Hạn mức mới làm tổng phân bổ vượt quá tiền khả dụng của kỳ.");

        envelope.setName(envelopeName);
        envelope.setAllocatedAmount(amount);
        envelope.setRemainingAmount(amount.subtract(envelope.getSpentAmount()));
        budget.setTotalAllocated(updatedTotal);
        return budget.getId();
    }

    private boolean isOtherCategory(Category category) {
        return Boolean.TRUE.equals(category.getIsSystem()) && "Mục khác".equals(category.getName());
    }

    private String requireCustomEnvelopeName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Vui lòng nhập tên cho mục khác.");
        String normalizedName = name.trim();
        if (normalizedName.length() > 100)
            throw new IllegalArgumentException("Tên phong bì tối đa 100 ký tự.");
        return normalizedName;
    }

    @Transactional
    public void setFixed(Long envelopeId, boolean fixed) {
        requireEditorRole();
        Long workspaceId = workspaceContext.getCurrentWorkspaceId();
        Envelope envelope = envelopeRepository.findById(envelopeId)
                .filter(e -> e.getBudgetPeriod().getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phong bì."));
        if (envelope.getBudgetPeriod().getStatus() != BudgetStatus.CONFIRMED)
            throw new IllegalArgumentException("Chỉ có thể khóa/mở phong bì của kỳ đã xác nhận.");
        envelope.setFixedEnvelope(fixed);
    }

    private void requireEditorRole() {
        var role = workspaceContext.getCurrentUserRoleInWorkspace(workspaceContext.getCurrentWorkspaceId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không có quyền sửa quỹ này."));
        if (role.name().equals("VIEWER")) throw new AccessDeniedException("Quyền xem không cho phép sửa phong bì.");
    }
}

