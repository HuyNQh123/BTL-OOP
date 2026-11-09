package com.smartenvelope.dto.workspace;

import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.WorkspaceType;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * DTO tóm tắt thông tin Workspace cho danh sách hiển thị trên giao diện.
 * Phụ trách bởi: Thành viên 1.
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkspaceSummaryDTO {

    private Long id;
    private String name;
    private WorkspaceType type;
    private String description;
    private String ownerName;
    private Long ownerId;
    private long memberCount;
    private MemberRole currentUserRole;
    private boolean isCurrent;
    private LocalDateTime createdAt;

    public WorkspaceSummaryDTO(Long id, String name, WorkspaceType type, String description,
                               String ownerName, Long ownerId, long memberCount,
                               MemberRole currentUserRole, boolean isCurrent, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.memberCount = memberCount;
        this.currentUserRole = currentUserRole;
        this.isCurrent = isCurrent;
        this.createdAt = createdAt;
    }

}

