package com.smartenvelope.repository;

import com.smartenvelope.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu với bảng Account.
 * Phụ trách bởi: Thành viên 2.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // TODO: Thành viên 2 định nghĩa các query methods (findByUserId...)
}
