package by.shakhau.ps.order.controller.filter;

import by.shakhau.ps.order.service.impl.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter implements Filter {

    private final JwtService jwtService;

    @RequiredArgsConstructor
    @Getter
    public static class UserPrincipal implements UserDetails {

        private final UUID id;
        private final Collection<? extends GrantedAuthority> authorities;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String bearer = "Bearer ";
        String authHeader = ((HttpServletRequest) request).getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(bearer)) {
            String token = authHeader.substring(bearer.length());
            Claims claims = jwtService.getClaims(token);

            if (claims == null || claims.getExpiration().before(new Date())) {
                chain.doFilter(request, response);
                return;
            }

            UUID userId = UUID.fromString(claims.getSubject());
            List<String> roles = (List<String>) claims.get("roles");

            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            var principal = new UserPrincipal(userId, authorities);
            var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
