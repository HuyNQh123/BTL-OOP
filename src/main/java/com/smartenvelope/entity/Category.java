package com.smartenvelope.entity;

import jakarta.persistence.*;

/**
 * Entity đại diện cho danh mục thu chi (Ăn uống, Nhà ở, Mua sắm, v.v.).
 * Phụ trách bởi: Thành viên 3.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Thành viên 3 khai báo các thuộc tính (name, type, icon, isDefault, active, createdAt...)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

