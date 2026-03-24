package com.example.backend.config;

import com.example.backend.security.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // Return 0L or handle otherwise if accessed publicly without being logged in
            return Optional.of(0L);
        }

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Optional.of(userPrincipal.getId());
    }
}
