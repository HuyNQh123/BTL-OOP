package com.smartenvelope.service;

import com.smartenvelope.dto.transaction.TransactionRequest;
import com.smartenvelope.dto.transaction.TransactionResponse;
import com.smartenvelope.entity.*;
import com.smartenvelope.enums.*;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.*;
import com.smartenvelope.repository.UserFinanceSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final EnvelopeRepository envelopeRepository;
    private final BudgetPeriodRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final EnvelopeTransferRepository transferRepository;
    private final WorkspaceContextService workspaceContext;
    private final CurrentUserService currentUserService;
    private final UserFinanceSettingsRepository settingsRepository;

    public List<TransactionResponse> getCurrentWorkspaceTransactions() {
        return transactionRepository.findRecentTransactions(workspaceContext.getCurrentWorkspaceId())
                .stream().map(TransactionResponse::new).toList();
    }

    public List<Account> getCurrentWorkspaceAccounts() {
        return accountRepository.findByWorkspaceId(workspaceContext.getCurrentWorkspaceId());
    }

    public List<Envelope> getCurrentWorkspaceEnvelopes() {
        Long wid = workspaceContext.getCurrentWorkspaceId();
        return budgetRepository.findByWorkspaceIdOrderByYearDescMonthDesc(wid).stream()
                .filter(b -> b.getStatus() == BudgetStatus.CONFIRMED)
                .flatMap(b -> envelopeRepository.findByBudgetPeriodId(b.getId()).stream()).toList();
    }

    public boolean hasConfirmedBudgetFor(LocalDate date) {
        if (date == null) return false;
        return budgetRepository.findByWorkspaceIdAndMonthAndYear(workspaceContext.getCurrentWorkspaceId(),
                        date.getMonthValue(), date.getYear())
                .map(budget -> budget.getStatus() == BudgetStatus.CONFIRMED)
                .orElse(false);
    }

    public List<Category> getAvailableCategories() {
        return categoryRepository.findAllAvailableForUser(currentUserService.getCurrentUserId()).stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive())).toList();
    }

    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        requireWriterRole();
        Long workspaceId = workspaceContext.getCurrentWorkspaceId();
        BigDecimal amount = request.amount();
        if (amount.stripTrailingZeros().scale() > 0) throw new IllegalArgumentException("Số tiền phải là số nguyên đồng Việt Nam.");
        Account account = accountRepository.findById(request.accountId()).filter(a -> a.getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản trong quỹ hiện tại."));
        CategoryType expectedType = request.type() == TransactionType.INCOME ? CategoryType.INCOME : CategoryType.EXPENSE;
        Category category = categoryRepository.findById(request.categoryId())
                .filter(c -> c.getType() == expectedType && Boolean.TRUE.equals(c.getIsActive()))
                .filter(c -> Boolean.TRUE.equals(c.getIsSystem()) || (c.getUser() != null && c.getUser().getId().equals(currentUserService.getCurrentUserId())))
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không hợp lệ."));
        Envelope envelope = null;
        if (request.type() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(amount));
            var period = budgetRepository.findByWorkspaceIdAndMonthAndYear(workspaceId,
                    request.transactionDate().getMonthValue(), request.transactionDate().getYear());
            if (period.isPresent()) {
                BudgetPeriod budget = period.get();
                if (budget.getStatus() == BudgetStatus.CLOSED)
                    throw new IllegalArgumentException("Không thể ghi thêm thu nhập vào kỳ ngân sách đã đóng.");
                budget.setTotalIncome(budget.getTotalIncome().add(amount));
                // Thu nhập mới phải được phân bổ lại đủ trước khi được phép ghi chi.
                budget.setStatus(BudgetStatus.DRAFT);
            }
        } else {
            if (!hasConfirmedBudgetFor(request.transactionDate()))
                throw new IllegalArgumentException("Chỉ có thể ghi khoản chi sau khi ngân sách của kỳ giao dịch được xác nhận.");
            if (account.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Tài khoản không đủ số dư thực tế cho khoản chi.");
            if (isOtherCategory(category)) {
                if (request.sourceEnvelopeId() == null)
                    throw new IllegalArgumentException("Với danh mục Mục khác, hãy chọn phong bì dùng để bù khoản chi.");
                envelope = envelopeRepository.findById(request.sourceEnvelopeId())
                        .filter(e -> e.getBudgetPeriod().getWorkspace().getId().equals(workspaceId))
                        .filter(e -> e.getBudgetPeriod().getStatus() == BudgetStatus.CONFIRMED)
                        .filter(e -> !request.transactionDate().isBefore(e.getBudgetPeriod().getStartDate())
                                && !request.transactionDate().isAfter(e.getBudgetPeriod().getEndDate()))
                        .orElseThrow(() -> new IllegalArgumentException("Hãy chọn phong bì đã xác nhận thuộc kỳ giao dịch để bù."));
                if (Boolean.TRUE.equals(envelope.getFixedEnvelope()))
                    throw new IllegalArgumentException("Phong bì cố định đã khóa, không thể dùng để bù khoản chi.");
                if (envelope.getRemainingAmount().compareTo(amount) < 0)
                    throw new IllegalArgumentException("Phong bì được chọn không đủ số dư để bù toàn bộ khoản chi.");
            } else if (request.envelopeId() != null) {
                envelope = envelopeRepository.findById(request.envelopeId())
                        .filter(e -> e.getBudgetPeriod().getWorkspace().getId().equals(workspaceId))
                        .filter(e -> e.getBudgetPeriod().getStatus() == BudgetStatus.CONFIRMED)
                        .filter(e -> !request.transactionDate().isBefore(e.getBudgetPeriod().getStartDate())
                                && !request.transactionDate().isAfter(e.getBudgetPeriod().getEndDate()))
                        .orElseThrow(() -> new ResourceNotFoundException("Phong bì không thuộc kỳ ngân sách đã xác nhận phù hợp."));
                if (envelope.getCategory().getId().longValue() != category.getId().longValue())
                    throw new IllegalArgumentException("Danh mục giao dịch phải trùng với danh mục của phong bì.");
                Envelope selectedEnvelope = envelope;
                BigDecimal shortfall = amount.subtract(selectedEnvelope.getRemainingAmount()).max(BigDecimal.ZERO);
                if (shortfall.signum() > 0) {
                    if (request.sourceEnvelopeId() == null) throw new IllegalArgumentException("Phong bì thiếu tiền. Hãy chọn phong bì nguồn để bù trước khi ghi chi.");
                    Envelope source = envelopeRepository.findById(request.sourceEnvelopeId())
                            .filter(s -> s.getBudgetPeriod().getId().equals(selectedEnvelope.getBudgetPeriod().getId()))
                            .filter(s -> !s.getId().equals(selectedEnvelope.getId()))
                            .orElseThrow(() -> new IllegalArgumentException("Nguồn bù phải là phong bì khác trong cùng kỳ."));
                    if (source.getRemainingAmount().compareTo(shortfall) < 0)
                        throw new IllegalArgumentException("Phong bì nguồn không đủ số dư để bù khoản thiếu.");
                    if (Boolean.TRUE.equals(source.getFixedEnvelope()))
                        throw new IllegalArgumentException("Phong bì cố định đã khóa, không thể dùng làm nguồn bù.");
                    source.setAllocatedAmount(source.getAllocatedAmount().subtract(shortfall));
                    source.setRemainingAmount(source.getRemainingAmount().subtract(shortfall));
                    selectedEnvelope.setAllocatedAmount(selectedEnvelope.getAllocatedAmount().add(shortfall));
                    selectedEnvelope.setRemainingAmount(selectedEnvelope.getRemainingAmount().add(shortfall));
                    transferRepository.save(new EnvelopeTransfer(selectedEnvelope.getBudgetPeriod(), source, selectedEnvelope, shortfall,
                            TransferReason.COVER_EXPENSE, "Bù ngân sách cho khoản chi", currentUserService.getCurrentUser()));
                }
            } else {
                throw new IllegalArgumentException("Danh mục này cần có phong bì trong kỳ. Nếu chưa có, hãy chọn Mục khác và chọn phong bì bù.");
            }
            account.setBalance(account.getBalance().subtract(amount));
            envelope.setSpentAmount(envelope.getSpentAmount().add(amount));
            envelope.setRemainingAmount(envelope.getRemainingAmount().subtract(amount));
        }
        Transaction tx = new Transaction(workspaceContext.getCurrentWorkspace(), account, envelope, category,
                currentUserService.getCurrentUser(), amount, request.type(), request.transactionDate(), request.note());
        return transactionRepository.save(tx);
    }

    public BigDecimal getHourlyWageForCurrentUser() {
        return settingsRepository.findByUserId(currentUserService.getCurrentUserId())
                .map(UserFinanceSettings::getHourlyWage).orElse(BigDecimal.ZERO);
    }

    public BigDecimal effortHoursForCurrentUser(BigDecimal amount) {
        BigDecimal wage = getHourlyWageForCurrentUser();
        return wage.signum() <= 0 ? BigDecimal.ZERO : amount.divide(wage, 1, java.math.RoundingMode.HALF_UP);
    }

    private boolean isOtherCategory(Category category) {
        return Boolean.TRUE.equals(category.getIsSystem()) && "Mục khác".equals(category.getName());
    }

    private void requireWriterRole() {
        MemberRole role = workspaceContext.getCurrentUserRoleInWorkspace(workspaceContext.getCurrentWorkspaceId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không có quyền ghi giao dịch vào quỹ này."));
        if (role == MemberRole.VIEWER) throw new AccessDeniedException("Quyền xem không cho phép ghi giao dịch.");
    }
}

