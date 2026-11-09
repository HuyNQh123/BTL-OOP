package com.smartenvelope.entity;

import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho thành viên tham gia vào một workspace và quyền hạn tương ứng.
 * Phụ trách bởi: Thành viên 1 & Thành viên 2.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "memberships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workspace_id", "user_id"})
})
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role = MemberRole.VIEWER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.PENDING;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    public Membership(Workspace workspace, User user, MemberRole role, MembershipStatus status) {
        this.workspace = workspace;
        this.user = user;
        this.role = role;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = MemberRole.VIEWER;
        }
        if (this.status == null) {
            this.status = MembershipStatus.PENDING;
        }
    }

}

