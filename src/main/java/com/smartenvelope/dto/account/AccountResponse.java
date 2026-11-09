package com.smartenvelope.dto.account;

import com.smartenvelope.entity.Account;
import com.smartenvelope.enums.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(Long id, String name, AccountType type, BigDecimal balance, LocalDateTime createdAt) {
    public AccountResponse(Account account) {
        this(account.getId(), account.getName(), account.getType(), account.getBalance(), account.getCreatedAt());
    }
}
