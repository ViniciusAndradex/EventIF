package br.edu.ifce.eventif.config;

import br.edu.ifce.eventif.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

/** Ends sessions whose stored permissions no longer match the account. */
public class CurrentRoleFilter extends OncePerRequestFilter {
    private final UserService users;

    public CurrentRoleFilter(UserService users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            boolean current;
            try {
                UserDetails user = users.loadUserByUsername(authentication.getName());
                current = user.isEnabled()
                        && new HashSet<>(user.getAuthorities()).equals(new HashSet<>(authentication.getAuthorities()));
            } catch (UsernameNotFoundException exception) {
                current = false;
            }
            if (!current) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }
            }
        }
        chain.doFilter(request, response);
    }
}
