package com.smartenvelope.service;

import com.smartenvelope.dto.savings.*;
import com.smartenvelope.entity.*;
import com.smartenvelope.enums.*;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service xử lý nghiệp vụ mục tiêu tiết kiệm và tích lũy tiền vào heo đất điện
 * tử. Phụ trách bởi: Thành viên 4.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final PiggyBankRepository piggyBankRepository;
    private final PiggyBankEntryRepository entryRepository;
    private final WorkspaceContextService workspaceContext;
    private final EnvelopeRepository envelopeRepository;
    private final BudgetPeriodRepository budgetRepository;
    private final AccountRepository accountRepository;
    private final CurrentUserService currentUserService;
    private final UserFinanceSettingsRepository settingsRepository;
    private final MonthCloseRepository monthCloseRepository;

    public List<SavingsGoalResponse> getGoals() {

        return goalRepository.findByWorkspaceId(workspaceContext.getCurrentWorkspaceId()).stream()
                .map(SavingsGoalResponse::new).toList();
    }

    public BigDecimal getPiggyBankBalance() {

        return piggyBankRepository.findByWorkspaceId(workspaceContext.getCurrentWorkspaceId())
                .map(PiggyBank::getTotalBalance).orElse(BigDecimal.ZERO);
    }

    public SurplusInfo getPreviousMonthSurplusInfo() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        YearMonth previousMonth = YearMonth.from(today).minusMonths(1);
        Long workspaceId = workspaceContext.getCurrentWorkspaceId();
        int salaryDay = settingsRepository.findByUserId(currentUserService.getCurrentUserId())
                .map(UserFinanceSettings::getSalaryDay).orElse(5);
        LocalDate payday = today.withDayOfMonth(Math.min(salaryDay, today.lengthOfMonth()));
        String monthLabel = previousMonth.format(DateTimeFormatter.ofPattern("MM/yyyy"));

        BudgetPeriod budget = budgetRepository.findByWorkspaceIdAndMonthAndYear(
                workspaceId, previousMonth.getMonthValue(), previousMonth.getYear()).orElse(null);
        if (budget == null) {
            return new SurplusInfo(false, monthLabel, BigDecimal.ZERO,
                    "Chưa có ngân sách tháng trước để chuyển số dư.");
        }
        if (budget.getStatus() == BudgetStatus.CLOSED) {
            return new SurplusInfo(false, monthLabel, BigDecimal.ZERO,
                    "Ngân sách tháng trước đã được chốt.");
        }
        if (budget.getStatus() != BudgetStatus.CONFIRMED
                || budget.getRolloverPolicy() != RolloverPolicy.SWEEP_TO_PIGGY) {
            return new SurplusInfo(false, monthLabel, BigDecimal.ZERO,
                    "Ngân sách tháng trước chưa xác nhận hoặc không đặt chuyển tiền dư vào tiết kiệm.");
        }

        BigDecimal remaining = envelopeRepository.findByBudgetPeriodId(budget.getId()).stream()
                .map(Envelope::getRemainingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (remaining.signum() <= 0) {
            return new SurplusInfo(false, monthLabel, BigDecimal.ZERO,
                    "Ngân sách tháng trước không còn tiền dư để chuyển.");
        }
        if (today.isBefore(payday)) {
            return new SurplusInfo(false, monthLabel, remaining,
                    "Có thể chuyển tiền dư từ ngày nhận lương " + payday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
        }
        return new SurplusInfo(true, monthLabel, remaining,
                "Tiền dư sẽ được chuyển vào mục tiêu bạn chọn.");
    }

    @Transactional
    public SavingsGoal createGoal(SavingsGoalRequest request) {

        requireEditorRole();

        BigDecimal initialAmount = request.initialAmount() == null ? BigDecimal.ZERO
                : request.initialAmount();
        requireWholeVnd(initialAmount);
        requireWholeVnd(request.targetAmount());

        if (initialAmount.compareTo(request.targetAmount()) > 0) {

            throw new IllegalArgumentException("Số tiền ban đầu không được lớn hơn mục tiêu.");
        }

        requireAvailableFunds(initialAmount);

        Workspace workspace = workspaceContext.getCurrentWorkspace();

        SavingsGoal goal = new SavingsGoal(workspace, request.name().trim(), request.targetAmount(),
                request.targetDate());

        goal.setCurrentAmount(initialAmount);

        if (initialAmount.compareTo(request.targetAmount()) == 0) {

            goal.setStatus(SavingsGoalStatus.COMPLETED);
        }

        goal = goalRepository.save(goal);

        if (initialAmount.signum() > 0) {
            transferToPiggyBank(workspace, initialAmount, goal, null, PiggyBankEntryType.MANUAL_DEPOSIT,
                    "Nạp thủ công cho mục tiêu: " + goal.getName());
        }

        return goal;
    }

    @Transactional
    public void deposit(Long goalId, BigDecimal amount) {

        requireEditorRole();

        SavingsGoal goal = getGoalInCurrentWorkspace(goalId);

        if (amount == null || amount.signum() <= 0) {

            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0.");
        }
        requireWholeVnd(amount);

        if (goal.getStatus() != SavingsGoalStatus.ACTIVE) {

            throw new IllegalArgumentException("Chỉ có thể nạp vào mục tiêu đang hoạt động.");
        }

        requireAvailableFunds(amount);

        BigDecimal newAmount = goal.getCurrentAmount().add(amount);

        if (newAmount.compareTo(goal.getTargetAmount()) > 0) {

            throw new IllegalArgumentException(
                    "Số tiền nạp vượt quá số tiền còn thiếu của mục tiêu.");
        }

        goal.setCurrentAmount(newAmount);

        if (newAmount.compareTo(goal.getTargetAmount()) == 0) {

            goal.setStatus(SavingsGoalStatus.COMPLETED);
        }

        transferToPiggyBank(goal.getWorkspace(), amount, goal, null, PiggyBankEntryType.MANUAL_DEPOSIT,
                "Nạp thủ công cho mục tiêu: " + goal.getName());
    }

    @Transactional
    public BigDecimal sweepPreviousMonthSurplus(Long goalId) {
        requireEditorRole();
        SavingsGoal goal = getGoalInCurrentWorkspace(goalId);
        if (goal.getStatus() != SavingsGoalStatus.ACTIVE)
            throw new IllegalArgumentException("Chỉ có thể nhận tiền dư vào mục tiêu đang hoạt động.");

        SurplusInfo surplusInfo = getPreviousMonthSurplusInfo();
        if (!surplusInfo.eligible())
            throw new IllegalArgumentException(surplusInfo.message());
        BigDecimal goalRoom = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        if (goalRoom.signum() <= 0)
            throw new IllegalArgumentException("Mục tiêu này đã đạt số tiền cần thiết.");

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        YearMonth previousMonth = YearMonth.from(today).minusMonths(1);
        BudgetPeriod budget = budgetRepository.findByWorkspaceIdAndMonthAndYear(
                workspaceContext.getCurrentWorkspaceId(), previousMonth.getMonthValue(), previousMonth.getYear())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ngân sách tháng trước."));
        BigDecimal transferAmount = surplusInfo.amount().min(goalRoom);
        BigDecimal leftToTransfer = transferAmount;
        Workspace workspace = goal.getWorkspace();
        PiggyBank bank = piggyBankRepository.findByWorkspaceId(workspace.getId()).orElseGet(
                () -> piggyBankRepository.save(new PiggyBank(workspace, BigDecimal.ZERO)));

        for (Envelope envelope : envelopeRepository.findByBudgetPeriodId(budget.getId())) {
            if (leftToTransfer.signum() == 0) break;
            BigDecimal amount = envelope.getRemainingAmount().min(leftToTransfer);
            if (amount.signum() <= 0) continue;
            envelope.setAllocatedAmount(envelope.getAllocatedAmount().subtract(amount));
            envelope.setRemainingAmount(envelope.getRemainingAmount().subtract(amount));
            budget.setTotalAllocated(budget.getTotalAllocated().subtract(amount));
            bank.setTotalBalance(bank.getTotalBalance().add(amount));
            entryRepository.save(new PiggyBankEntry(bank, amount, PiggyBankEntryType.SWEEP,
                    envelope, goal, "Chuyển tiền dư tháng " + surplusInfo.monthLabel() + " vào mục tiêu: " + goal.getName()));
            leftToTransfer = leftToTransfer.subtract(amount);
        }

        goal.setCurrentAmount(goal.getCurrentAmount().add(transferAmount));
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0)
            goal.setStatus(SavingsGoalStatus.COMPLETED);

        BigDecimal spent = envelopeRepository.findByBudgetPeriodId(budget.getId()).stream()
                .map(Envelope::getSpentAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        MonthClose close = monthCloseRepository.findByBudgetPeriodId(budget.getId())
                .orElseGet(() -> new MonthClose(budget, currentUserService.getCurrentUser(), spent,
                        BigDecimal.ZERO, "Chuyển tiền dư vào mục tiêu tiết kiệm."));
        close.setTotalSaved(close.getTotalSaved().add(transferAmount));
        monthCloseRepository.save(close);

        BigDecimal remainder = surplusInfo.amount().subtract(transferAmount);
        if (remainder.signum() == 0) budget.setStatus(BudgetStatus.CLOSED);
        return transferAmount;
    }

    /** Quét số dư phong bì không bị khóa vào heo đất sau khi kỳ đã kết thúc. */
    @Transactional
    public void closeExpiredPeriods() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        for (BudgetPeriod budget : budgetRepository.findAll()) {
            if (budget.getStatus() != BudgetStatus.CONFIRMED || !budget.getEndDate().isBefore(today)
                    || budget.getRolloverPolicy() != RolloverPolicy.SWEEP_TO_PIGGY
                    || monthCloseRepository.existsByBudgetPeriodId(budget.getId())) continue;
            List<Envelope> envelopes = envelopeRepository.findByBudgetPeriodId(budget.getId());
            BigDecimal swept = BigDecimal.ZERO;
            for (Envelope envelope : envelopes) {
                BigDecimal amount = envelope.getRemainingAmount().max(BigDecimal.ZERO);
                if (amount.signum() == 0) continue;
                envelope.setAllocatedAmount(envelope.getAllocatedAmount().subtract(amount));
                envelope.setRemainingAmount(BigDecimal.ZERO);
                budget.setTotalAllocated(budget.getTotalAllocated().subtract(amount));
                transferToPiggyBank(budget.getWorkspace(), amount, null, envelope, PiggyBankEntryType.SWEEP,
                        "Tự động gom tiền dư cuối kỳ: " + budget.getName());
                swept = swept.add(amount);
            }
            BigDecimal spent = envelopes.stream().map(Envelope::getSpentAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            monthCloseRepository.save(new MonthClose(budget, budget.getWorkspace().getOwner(), spent, swept,
                    "Tự động chuyển số dư phong bì chưa khóa vào Lợn đất tiết kiệm."));
            budget.setStatus(BudgetStatus.CLOSED);
        }
    }

    private void requireAvailableFunds(BigDecimal amount) {
        BigDecimal accountBalance = accountRepository.findByWorkspaceId(workspaceContext.getCurrentWorkspaceId())
                .stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal alreadySaved = piggyBankRepository.findByWorkspaceId(workspaceContext.getCurrentWorkspaceId())
                .map(PiggyBank::getTotalBalance).orElse(BigDecimal.ZERO);
        if (amount.compareTo(accountBalance.subtract(alreadySaved).max(BigDecimal.ZERO)) > 0)
            throw new IllegalArgumentException("Số dư tài khoản khả dụng không đủ để nạp khoản tiết kiệm này.");
    }

    private void requireWholeVnd(BigDecimal amount) {
        if (amount.stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("Số tiền phải là số nguyên đồng Việt Nam.");
    }

    private SavingsGoal getGoalInCurrentWorkspace(Long goalId) {

        Long workspaceId = workspaceContext.getCurrentWorkspaceId();

        return goalRepository.findById(goalId)
                .filter(goal -> goal.getWorkspace().getId().equals(workspaceId)).orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy mục tiêu tiết kiệm."));
    }

    private void requireEditorRole() {

        MemberRole role = workspaceContext
                .getCurrentUserRoleInWorkspace(workspaceContext.getCurrentWorkspaceId())
                .orElseThrow(() -> new AccessDeniedException(
                        "Bạn không có quyền sửa dữ liệu tiết kiệm."));

        if (role == MemberRole.VIEWER) {

            throw new AccessDeniedException("Quyền xem không cho phép sửa dữ liệu tiết kiệm.");
        }
    }

    private void transferToPiggyBank(Workspace workspace, BigDecimal amount, SavingsGoal goal,
            Envelope source, PiggyBankEntryType type, String note) {

        PiggyBank bank = piggyBankRepository.findByWorkspaceId(workspace.getId()).orElseGet(
                () -> piggyBankRepository.save(new PiggyBank(workspace, BigDecimal.ZERO)));

        bank.setTotalBalance(bank.getTotalBalance().add(amount));

        entryRepository.save(new PiggyBankEntry(bank, amount, type, source, goal, note));
    }

    public record SurplusInfo(boolean eligible, String monthLabel, BigDecimal amount, String message) { }
}
