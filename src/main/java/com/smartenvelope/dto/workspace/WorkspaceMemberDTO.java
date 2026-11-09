package com.smartenvelope.dto.workspace;

import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * DTO biểu diễn thông tin thành viên trong Quỹ để hiển thị danh sách và phân quyền.
 * Phụ trách bởi: Thành viên 1.
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkspaceMemberDTO {

    private Long membershipId;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String avatarUrl;
    private MemberRole role;
    private MembershipStatus status;
    private LocalDateTime joinedAt;
    private boolean isOwner;
    private boolean canBeManaged;
    private boolean canBeRemoved;

    public WorkspaceMemberDTO(Long membershipId, Long userId, String username, String fullName,
                              String email, String avatarUrl, MemberRole role,
                              MembershipStatus status, LocalDateTime joinedAt,
                              boolean isOwner, boolean canBeManaged, boolean canBeRemoved) {
        this.membershipId = membershipId;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
        this.isOwner = isOwner;
        this.canBeManaged = canBeManaged;
        this.canBeRemoved = canBeRemoved;
    }

}

