package com.smartenvelope.service;

import com.smartenvelope.dto.auth.RegisterRequest;
import com.smartenvelope.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;

    public boolean existsByEmail(String email) { return userService.existsByEmail(email); }
    public boolean existsByUsername(String username) { return userService.existsByUsername(username); }

    @Transactional
    public User register(RegisterRequest request) { return userService.register(request); }
}

