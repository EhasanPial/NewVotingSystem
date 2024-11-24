package org.example.onlinevotingsystem.auth;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException {
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Log the roles for debugging
        System.out.println("User roles: " + authorities);
        
        // Determine redirect URL based on role
        String redirectUrl = determineTargetUrl(authorities);
        
        // Redirect to appropriate URL
        response.sendRedirect(redirectUrl);
    }
    
    private String determineTargetUrl(Collection<? extends GrantedAuthority> authorities) {
        boolean isAdmin = authorities.stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        boolean isUser = authorities.stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));
            
        if (isAdmin) {
            return "/admin-index";
        } else if (isUser) {
            return "/polls";
        } else {
            return "/"; // default landing page
        }
    }
}