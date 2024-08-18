package com.taskmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class TokenFilter extends OncePerRequestFilter {
  private JwtCore jwtCore;
  private UserDetailsService userDetailsService;

  @Autowired
  public void setUserDetailsService(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Autowired
  public void setJwtCore(JwtCore jwtCore) {
    this.jwtCore = jwtCore;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String jwt = null;
    String username = null;
    UserDetails userDetails = null;
    UsernamePasswordAuthenticationToken auth = null;
    String uri = request.getRequestURI();
    if (!(uri.equals("/auth/signin"))
            && !(uri.equals("/auth/signup"))
            && !(uri.startsWith("/swagger"))
            && !(uri.startsWith("/v3/api-docs"))) {
      jwt = jwtCore.getTokenFromRequest(request);
      try {
        username = jwtCore.getNameFromJwt(jwt);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          userDetails = userDetailsService.loadUserByUsername(username);
          auth =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception e) {
        log.error("Invalid JWT token");
      }
    }
    filterChain.doFilter(request, response);
  }
}
