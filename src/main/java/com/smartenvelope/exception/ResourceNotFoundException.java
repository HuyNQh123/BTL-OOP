package com.smartenvelope.exception;

/**
 * Ngoại lệ ném ra khi không tìm thấy tài nguyên trong hệ thống (User, Account, Envelope, Transaction...).
 * Phụ trách bởi: Toàn nhóm (Dùng chung).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

