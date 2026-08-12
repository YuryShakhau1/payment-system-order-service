package by.shakhau.ps.order.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

@AllArgsConstructor
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String SESSION_ID_HEADER = "X-Session-Id";

    @AllArgsConstructor
    @Getter
    public static class UserPrincipal implements UserDetails {

        private final UUID id;
        private final UUID sessionId;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public String getUsername() {
            return String.valueOf(id);
        }
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String sessionIdHeader = request.getHeader(SESSION_ID_HEADER);

        if (userIdHeader != null && sessionIdHeader != null) {
            UUID userId = UUID.fromString(userIdHeader);
            UUID sessionId = UUID.fromString(sessionIdHeader);

            UserPrincipal principal = new UserPrincipal(userId, sessionId);
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, null);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
