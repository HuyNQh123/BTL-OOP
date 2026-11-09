package com.smartenvelope.entity;

import com.smartenvelope.enums.TransactionStatus;
import com.smartenvelope.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho giao dịch thu/chi tài chính.
 * Giao dịch chi tiêu (EXPENSE) gắn liền với một phong bì (envelope_id) và tài khoản nguồn.
 * Giao dịch thu nhập (INCOME) gắn với tài khoản nguồn, envelope_id có thể là null.
 * Hỗ trợ Optimistic Locking qua version.
 * Phụ trách bởi: Thành viên 3 (Transaction & Ledger Processing).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelope_id")
    private Envelope envelope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.COMPLETED;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "non_essential", nullable = false)
    private Boolean nonEssential = false;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Transaction(Workspace workspace, Account account, Envelope envelope, Category category,
                       User user, BigDecimal amount, TransactionType type, LocalDate transactionDate, String note) {
        this.workspace = workspace;
        this.account = account;
        this.envelope = envelope;
        this.category = category;
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.transactionDate = transactionDate;
        this.note = note;
        this.status = TransactionStatus.COMPLETED;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TransactionStatus.COMPLETED;
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
