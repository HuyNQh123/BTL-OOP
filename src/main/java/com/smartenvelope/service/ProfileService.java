package com.smartenvelope.service;

import com.smartenvelope.dto.user.ProfileUpdateRequest;
import com.smartenvelope.entity.User;
import com.smartenvelope.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public void updateProfile(ProfileUpdateRequest request) {
        User user = currentUserService.getCurrentUser();
        user.setFullName(request.fullName().trim());
        String phone = request.phone() == null ? null : request.phone().trim();
        user.setPhone(phone == null || phone.isEmpty() ? null : phone);
        userRepository.save(user);
    }
}
