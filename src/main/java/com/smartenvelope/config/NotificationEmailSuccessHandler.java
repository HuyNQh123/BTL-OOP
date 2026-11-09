package com.smartenvelope.config;

import com.smartenvelope.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Sends users to notification email verification after login until a destination is verified. */
@Component
public class NotificationEmailSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public NotificationEmailSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        boolean verified = userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(authentication.getName()))
                .map(user -> user.isNotificationEmailVerified()
                        && user.getNotificationEmail() != null && !user.getNotificationEmail().isBlank())
                .orElse(false);
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response,
                verified ? "/dashboard" : "/alerts?setupEmail=true#notification-email");
    }
}
