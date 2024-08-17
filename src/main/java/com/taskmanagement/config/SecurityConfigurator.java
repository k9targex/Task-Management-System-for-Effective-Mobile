package com.taskmanagement.config;

import com.taskmanagement.security.TokenFilter;
import com.taskmanagement.service.UserService;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@Data
public class SecurityConfigurator {
  private TokenFilter tokenFilter;
  private UserService userService;
  private MyAuthenticationEntryPoint authenticationEntryPoint;
  private MyAccessDeniedHandler accessDeniedHandler;

  @Autowired
  public void setMyAuthenticationEntryPoint(MyAuthenticationEntryPoint authenticationEntryPoint) {
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Autowired
  public void setMyAccessDeniedHandler(MyAccessDeniedHandler accessDeniedHandler) {
    this.accessDeniedHandler = accessDeniedHandler;
  }

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  @Autowired
  public void setTokenFilter(TokenFilter tokenFilter) {
    this.tokenFilter = tokenFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  @Primary
  public AuthenticationManagerBuilder configureAuthenticationManagerBuilder(
      AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
    authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder());
    return authenticationManagerBuilder;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // Отключение CSRF-защиты. Используются jwt токены.
    http.csrf(AbstractHttpConfigurer::disable);
    http.cors(
            cors ->
                cors.configurationSource(
                    request -> {
                      CorsConfiguration configuration = new CorsConfiguration();
                      configuration.setAllowedOrigins(
                          List.of("http://localhost:8080")); // Разрешенные источники
                      configuration.setAllowedMethods(
                          Arrays.asList("GET", "POST", "PUT", "DELETE")); // Разрешенные методы
                      configuration.setAllowedHeaders(
                          Arrays.asList("Authorization", "Content-Type")); // Разрешенные заголовки
                      configuration.setAllowCredentials(true); // Разрешение использования куки
                      return configuration;
                    }))
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(authenticationEntryPoint) // для обработки 401
                    .accessDeniedHandler(accessDeniedHandler)) // для обработки 403
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.GET, "/users")
                    .hasAuthority("AUTHOR")
                    .requestMatchers(HttpMethod.POST, "/users/task")
                    .hasAuthority("AUTHOR")
                    .requestMatchers(HttpMethod.PATCH, "/users/task/{task_id}/{performer_id}")
                    .hasAuthority("AUTHOR")
                    .requestMatchers(HttpMethod.DELETE, "/users/task/{task_id}")
                    .hasAuthority("AUTHOR")
                    .requestMatchers(HttpMethod.GET, "/users/task")
                    .hasAnyAuthority("AUTHOR", "PERFORMER")
                    .requestMatchers(HttpMethod.GET, "/users/task/{user_id}")
                    .hasAnyAuthority("AUTHOR", "PERFORMER")
                    .requestMatchers(HttpMethod., "/users/task/{task_id}")
                    .hasAnyAuthority("AUTHOR", "PERFORMER")
                    .anyRequest()
                    .permitAll())
        .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
