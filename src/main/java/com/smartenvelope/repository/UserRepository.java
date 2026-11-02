package com.smartenvelope.repository;

import com.smartenvelope.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu với bảng User.
 * Phụ trách bởi: Thành viên 1.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // TODO: Thành viên 1 định nghĩa các query methods (findByEmail, existsByEmail...)
}

