package com.smartenvelope.service;

import com.smartenvelope.dto.budget.*;
import com.smartenvelope.entity.*;
import com.smartenvelope.enums.*;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý nghiệp vụ ngân sách và quy tắc tài chính (ví dụ quy tắc
 * 50/30/20, thiết lập hạn mức tổng). Phụ trách bởi: Thành viên 4.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetPeriodRepository budgetRepository;
    private final EnvelopeRepository envelopeRepository;
    private final WorkspaceContextService workspaceContext;
    private final AccountRepository accountRepository;
    private final PiggyBankRepository piggyBankRepository;
    private final TransactionRepository transactionRepository;
    private final EnvelopeTransferRepository transferRepository;
    private final PiggyBankEntryRepository piggyBankEntryRepository;
    private final UserFinanceSettingsRepository settingsRepository;
    private final CurrentUserService currentUserService;

    public BigDecimal getExpectedSalary() {
        return settingsRepository.findByUserId(currentUserService.getCurrentUserId())
                .map(UserFinanceSettings::getExpectedSalary).orElse(BigDecimal.ZERO);
    }

    /** Recommends category allocation from up to the three latest confirmed periods in this workspace. */
    public Map<String, BigDecimal> suggestAllocations(BigDecimal income) {
        if (income == null || income.signum() <= 0) return Map.of();
        List<BudgetPeriod> history = budgetRepository
                .findByWorkspaceIdOrderByYearDescMonthDesc(workspaceContext.getCurrentWorkspaceId()).stream()
                .filter(period -> period.getStatus() == BudgetStatus.CONFIRMED)
                .limit(3).toList();
        if (history.isEmpty()) return Map.of();

        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        BigDecimal historicalAllocations = BigDecimal.ZERO;
        for (BudgetPeriod period : history) {
            for (Envelope envelope : envelopeRepository.findByBudgetPeriodId(period.getId())) {
                String category = envelope.getCategory().getName();
                categoryTotals.merge(category, envelope.getAllocatedAmount(), BigDecimal::add);
                historicalAllocations = historicalAllocations.add(envelope.getAllocatedAmount());
            }
        }
        if (historicalAllocations.signum() <= 0) return Map.of();

        BigDecimal total = historicalAllocations;
        Map<String, BigDecimal> exact = new LinkedHashMap<>();
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal roundedTotal = BigDecimal.ZERO;
        for (var entry : categoryTotals.entrySet()) {
            BigDecimal share = entry.getValue().multiply(income).divide(total, 12, RoundingMode.HALF_UP);
            exact.put(entry.getKey(), share);
            BigDecimal rounded = share.setScale(0, RoundingMode.DOWN);
            result.put(entry.getKey(), rounded);
            roundedTotal = roundedTotal.add(rounded);
        }
        int remaining = income.subtract(roundedTotal).intValue();
        exact.entrySet().stream().sorted((left, right) -> right.getValue().remainder(BigDecimal.ONE)
                        .compareTo(left.getValue().remainder(BigDecimal.ONE)))
                .limit(Math.max(0, remaining)).forEach(entry -> result.computeIfPresent(entry.getKey(),
                        (key, value) -> value.add(BigDecimal.ONE)));
        return result;
    }

    public List<BudgetResponse> getCurrentWorkspaceBudgets() {

        return budgetRepository
                .findByWorkspaceIdOrderByYearDescMonthDesc(workspaceContext.getCurrentWorkspaceId())
                .stream().map(BudgetResponse::new).toList();
    }

    public BudgetPeriod getBudget(Long id) {

        Long workspaceId = workspaceContext.getCurrentWorkspaceId();

        return budgetRepository.findById(id)
                .filter(budget -> budget.getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kỳ ngân sách."));
    }

    @Transactional
    public BudgetPeriod createBudget(BudgetRequest request) {

        requireEditorRole();

        validateAvailableFunds(request.totalIncome());

        Workspace workspace = workspaceContext.getCurrentWorkspace();

        if (budgetRepository.findByWorkspaceIdAndMonthAndYear(workspace.getId(), request.month(),
                request.year()).isPresent()) {

            throw new IllegalArgumentException("Kỳ ngân sách này đã tồn tại.");
        }

        YearMonth month = YearMonth.of(request.year(), request.month());

        BudgetPeriod budget = new BudgetPeriod(workspace, request.name().trim(),
                month.getMonthValue(), month.getYear(), month.atDay(1), month.atEndOfMonth(),
                request.totalIncome());

        if (request.rolloverPolicy() != null) {

            budget.setRolloverPolicy(request.rolloverPolicy());
        }

        return budgetRepository.save(budget);
    }

    @Transactional
    public BudgetPeriod updateDraftBudget(Long budgetId, BudgetRequest request) {
        requireEditorRole();
        BudgetPeriod budget = getBudget(budgetId);
        if (budget.getStatus() != BudgetStatus.DRAFT)
            throw new IllegalArgumentException("Chỉ có thể sửa kỳ ngân sách đang ở trạng thái nháp.");

        BigDecimal allocated = envelopeRepository.findByBudgetPeriodId(budgetId).stream()
                .map(Envelope::getAllocatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (request.totalIncome().compareTo(allocated) < 0)
            throw new IllegalArgumentException("Tiền khả dụng mới thấp hơn số đã phân bổ. Hãy xóa hoặc điều chỉnh phong bì trước.");
        validateAvailableFunds(request.totalIncome());

        budgetRepository.findByWorkspaceIdAndMonthAndYear(budget.getWorkspace().getId(), request.month(), request.year())
                .filter(existing -> !existing.getId().equals(budgetId))
                .ifPresent(existing -> { throw new IllegalArgumentException("Đã có kỳ ngân sách cho tháng/năm này."); });

        YearMonth month = YearMonth.of(request.year(), request.month());
        budget.setName(request.name().trim());
        budget.setMonth(month.getMonthValue());
        budget.setYear(month.getYear());
        budget.setStartDate(month.atDay(1));
        budget.setEndDate(month.atEndOfMonth());
        budget.setTotalIncome(request.totalIncome());
        budget.setTotalAllocated(allocated);
        budget.setRolloverPolicy(request.rolloverPolicy());
        return budget;
    }

    @Transactional
    public void deleteDraftBudget(Long budgetId) {
        requireEditorRole();
        BudgetPeriod budget = getBudget(budgetId);
        if (budget.getStatus() != BudgetStatus.DRAFT)
            throw new IllegalArgumentException("Chỉ có thể xóa kỳ ngân sách đang ở trạng thái nháp.");
        List<Envelope> envelopes = envelopeRepository.findByBudgetPeriodId(budgetId);
        boolean hasHistory = envelopes.stream().anyMatch(envelope ->
                transactionRepository.existsByEnvelopeId(envelope.getId())
                        || piggyBankEntryRepository.existsBySourceEnvelopeId(envelope.getId()));
        if (hasHistory || !transferRepository.findByBudgetPeriodId(budgetId).isEmpty())
            throw new IllegalArgumentException("Không thể xóa kỳ đã có giao dịch hoặc lịch sử điều chuyển.");
        envelopeRepository.deleteAll(envelopes);
        budgetRepository.delete(budget);
    }

    @Transactional
    public void confirmBudget(Long budgetId) {

        requireEditorRole();

        BudgetPeriod budget = getBudget(budgetId);

        if (budget.getStatus() != BudgetStatus.DRAFT) {

            throw new IllegalArgumentException(
                    "Chỉ có thể xác nhận kỳ ngân sách đang ở trạng thái nháp.");
        }

        BigDecimal allocated = envelopeRepository.findByBudgetPeriodId(budgetId).stream()
                .map(Envelope::getAllocatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (allocated.compareTo(budget.getTotalIncome()) != 0) {
            BigDecimal difference = budget.getTotalIncome().subtract(allocated).abs();
            String formattedDifference = java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(difference);
            if (allocated.compareTo(budget.getTotalIncome()) < 0) {
                throw new IllegalArgumentException("Còn thiếu " + formattedDifference
                        + " ₫ chưa phân bổ. Mở “Phong bì” của kỳ này, thêm hoặc chỉnh phân bổ rồi quay lại xác nhận.");
            }
            throw new IllegalArgumentException("Đã phân bổ vượt " + formattedDifference
                    + " ₫. Hãy xóa phong bì tạo nhầm hoặc sửa lại tiền khả dụng trước khi xác nhận.");
        }

        budget.setTotalAllocated(allocated);
        budget.setStatus(BudgetStatus.CONFIRMED);
    }

    private void requireEditorRole() {

        MemberRole role = workspaceContext
                .getCurrentUserRoleInWorkspace(workspaceContext.getCurrentWorkspaceId())
                .orElseThrow(() -> new AccessDeniedException(
                        "Bạn không có quyền sửa kỳ ngân sách này."));

        if (role == MemberRole.VIEWER) {

            throw new AccessDeniedException("Quyền xem không cho phép sửa kỳ ngân sách.");
        }
    }

    private void validateAvailableFunds(BigDecimal amount) {
        if (amount.stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("Tiền khả dụng phải là số nguyên đồng Việt Nam.");
        Long workspaceId = workspaceContext.getCurrentWorkspaceId();
        BigDecimal accountFunds = accountRepository.findByWorkspaceId(workspaceId).stream()
                .map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal protectedSavings = piggyBankRepository.findByWorkspaceId(workspaceId)
                .map(PiggyBank::getTotalBalance).orElse(BigDecimal.ZERO);
        BigDecimal availableFunds = accountFunds.subtract(protectedSavings).max(BigDecimal.ZERO);
        if (amount.compareTo(availableFunds) > 0)
            throw new IllegalArgumentException("Không phải ngân sách phải lớn hơn heo đất. Kỳ này bạn có thể phân bổ tối đa "
                    + formatVnd(availableFunds) + " ₫ (số dư tài khoản " + formatVnd(accountFunds)
                    + " ₫ trừ " + formatVnd(protectedSavings) + " ₫ đã dành trong heo đất).");
    }

    private String formatVnd(BigDecimal amount) {
        return java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("vi-VN")).format(amount);
    }
}
