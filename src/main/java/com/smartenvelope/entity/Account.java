package com.smartenvelope.entity;

import com.smartenvelope.enums.AccountType;
import jakarta.persistence.*;
// import lombok.NoArgsConstructor;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho tài khoản tài chính (ngân hàng, tiền mặt, thẻ tín dụng,
 * tiết kiệm). Lưu giữ số dư thực tế theo đơn vị VND (BigDecimal scale 0) và hỗ
 * trợ Optimistic Locking qua trường version. Phụ trách bởi: Thành viên 2
 * (Account & Category Management).
 */
@Entity
@Table(name = "accounts")
@Setter
@Getter
// @NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AccountType type = AccountType.BANK;

    @Column(name = "balance", nullable = false, precision = 19, scale = 0)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "VND";

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Account() {
    }

    public Account(Workspace workspace, String name, AccountType type, BigDecimal balance,
            String currency) {
        this.workspace = workspace;
        this.name = name;
        this.type = type;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "VND";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        if (this.currency == null) {
            this.currency = "VND";
        }
        if (this.version == null) {
            this.version = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
