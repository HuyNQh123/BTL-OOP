package com.smartenvelope.service;

import com.smartenvelope.dto.account.AccountRequest;
import com.smartenvelope.dto.account.AccountResponse;
import com.smartenvelope.entity.Account;
import com.smartenvelope.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {
    private final AccountRepository accountRepository;
    private final WorkspaceContextService workspaceContext;

    public List<AccountResponse> getCurrentWorkspaceAccounts() {
        return accountRepository.findByWorkspaceId(workspaceContext.getCurrentWorkspaceId())
                .stream().map(AccountResponse::new).toList();
    }

    @Transactional
    public Account createAccount(AccountRequest request) {
        requireEditorRole();
        if (request.initialBalance().scale() > 0 && request.initialBalance().stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Số tiền phải là số nguyên đồng Việt Nam.");
        }
        Account account = new Account(workspaceContext.getCurrentWorkspace(), request.name().trim(),
                request.type(), request.initialBalance(), "VND");
        return accountRepository.save(account);
    }

    private void requireEditorRole() {
        var role = workspaceContext.getCurrentUserRoleInWorkspace(workspaceContext.getCurrentWorkspaceId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không có quyền sửa quỹ này."));
        if (role.name().equals("VIEWER")) throw new AccessDeniedException("Quyền xem không cho phép sửa tài khoản.");
    }
}
