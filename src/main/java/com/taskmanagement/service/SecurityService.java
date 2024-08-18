package com.taskmanagement.service;

import com.taskmanagement.dao.UserRepository;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.model.dto.SignInRequest;
import com.taskmanagement.model.dto.SignUpRequest;
import com.taskmanagement.model.entity.User;
import com.taskmanagement.security.JwtCore;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SecurityService {
  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private AuthenticationManager authenticationManager;
  private JwtCore jwtCore;
  private static final String USER_ALREADY_TAKEN = "This name already taken";

  private static final String EMAIL_ALREADY_TAKEN = "This name already taken";
  private static final String INCORRECT_CREDENTIALS ="Incorrect email or password";


  @Autowired
  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Autowired
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Autowired
  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @Autowired
  public void setJwtCore(JwtCore jwtCore) {
    this.jwtCore = jwtCore;
  }

  public String register(SignUpRequest signUpRequest) {
    if (userRepository.existsUserByUsername(signUpRequest.getUsername()).booleanValue()) {
      throw new UnauthorizedException(
              USER_ALREADY_TAKEN);
    }
    if (userRepository.existsUserByEmail(signUpRequest.getEmail()).booleanValue()) {
      throw new UnauthorizedException(
              EMAIL_ALREADY_TAKEN);
    }
    User user =
        User.builder()
            .username(signUpRequest.getUsername())
            .password(passwordEncoder.encode(signUpRequest.getPassword()))
            .email(signUpRequest.getEmail())
            .role(signUpRequest.getRole())
            .build();

    userRepository.save(user);
    Authentication authentication = null;
    authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                signUpRequest.getUsername(), signUpRequest.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return jwtCore.generateToken(authentication);
  }

  public String login(SignInRequest signInRequest) {
    Authentication authentication = null;
    try {
      authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  userRepository
                      .findUsersByEmail(signInRequest.getEmail())
                      .orElseThrow(() -> new UnauthorizedException(INCORRECT_CREDENTIALS))
                              .getUsername(), signInRequest.getPassword()));
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException(INCORRECT_CREDENTIALS);
    }
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return (jwtCore.generateToken(authentication));
  }
}
