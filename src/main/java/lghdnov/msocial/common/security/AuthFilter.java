package lghdnov.msocial.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lghdnov.msocial.feature.auth.api.TokenValidationPort;
import lghdnov.msocial.feature.auth.entity.JwtClaims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Фильтр аутентификации на основе JWT.
 *
 * <p>
 * Извлекает access-токен из заголовка {@code Authorization},
 * валидирует его через {@link TokenValidationPort} и устанавливает
 * контекст безопасности Spring Security.
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

  private final TokenValidationPort tokenValidationPort;

  AuthFilter(TokenValidationPort tokenValidationPort) {
    this.tokenValidationPort = tokenValidationPort;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      if (tokenValidationPort.validateToken(token)) {
        JwtClaims claims = tokenValidationPort.extractClaims(token);
        List<SimpleGrantedAuthority> authorities = claims.roles().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(claims.sub(), null,
            authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/v1/auth/")
        || path.startsWith("/api/echo/")
        || path.startsWith("/swagger-ui.html")
        || path.startsWith("/swagger-ui/")
        || path.startsWith("/scalar/")
        || path.startsWith("/v3/api-docs/")
        || path.startsWith("/actuator/");
  }
}
