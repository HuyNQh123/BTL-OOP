package com.smartenvelope.service;

import com.smartenvelope.dto.admin.AdminUserResponse;
import com.smartenvelope.entity.Category;
import com.smartenvelope.entity.User;
import com.smartenvelope.enums.CategoryType;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserService userService;
    private final CategoryService categoryService;
    public Page<User> searchUsers(String keyword, Role role, UserStatus status, Pageable pageable) {
        return userService.searchUsers(keyword, role, status, pageable);
    }
    public AdminUserResponse getUserDetail(Long userId) { return userService.getUserDetail(userId); }
    public void lockUser(Long userId, Long adminId) { userService.lockUser(userId, adminId); }
    public void unlockUser(Long userId, Long adminId) { userService.unlockUser(userId, adminId); }
    public List<Category> getCategories(CategoryType type, Boolean active, String keyword) {
        return categoryService.getSystemCategories(type, active, keyword);
    }
}

