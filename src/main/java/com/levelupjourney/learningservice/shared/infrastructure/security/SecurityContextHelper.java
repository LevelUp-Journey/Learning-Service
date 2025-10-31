package com.levelupjourney.learningservice.shared.infrastructure.security;

import com.levelupjourney.learningservice.shared.infrastructure.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;

@Component
public class SecurityContextHelper {

    public String getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object userId = request.getAttribute("userId");
            if (userId != null) {
                return userId.toString();
            }
        }
        return null;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    public Collection<? extends GrantedAuthority> getCurrentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities();
        }
        return null;
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(role));
        }
        return false;
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isTeacher() {
        return hasRole("ROLE_TEACHER");
    }

    public boolean isStudent() {
        return hasRole("ROLE_STUDENT");
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !authentication.getPrincipal().equals("anonymousUser");
    }
    
    public void requireAuthentication() {
        if (!isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }
    }
    
    public void requireRole(String role) {
        if (!hasRole(role)) {
            throw new UnauthorizedException("Role " + role + " required");
        }
    }
    
    public void requireAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return;
            }
        }
        throw new UnauthorizedException("One of the following roles required: " + String.join(", ", roles));
    }
}
