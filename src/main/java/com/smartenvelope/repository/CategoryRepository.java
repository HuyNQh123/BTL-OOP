package com.smartenvelope.repository;

import com.smartenvelope.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu với bảng Category.
 * Phụ trách bởi: Thành viên 3.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // TODO: Thành viên 3 định nghĩa các query methods (findByType, findByActiveTrue...)
}

