package com.taskmanagement.controller;

import com.taskmanagement.model.dto.SignInRequest;
import com.taskmanagement.model.dto.SignUpRequest;
import com.taskmanagement.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Tag(name = "Authentication", description = "Operations related to user authentication")
@RestController
@RequestMapping("/auth")
public class SecurityController {
  private SecurityService securityService;

  @Autowired
  public void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }


  @Operation(summary = "User Registration", description = "Register a new user")
  @ApiResponse(responseCode = "200", description = "User successfully registered")
  @ApiResponse(responseCode = "401", description = "User registration failed")
  @PostMapping("/signup")
  public ResponseEntity<String> signup(
      @Valid @RequestBody SignUpRequest signUpRequest, HttpServletResponse response) {

    String token = securityService.register(signUpRequest);
    setCookie(token, response);
    return ResponseEntity.ok("Successfully registered");
  }

  @Operation(summary = "User Sign-in", description = "Authenticate an existing user")
  @ApiResponse(responseCode = "200", description = "User successfully logged in")
  @ApiResponse(responseCode = "401", description = "User login failed")
  @CrossOrigin(origins = "/", allowCredentials = "true")
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/signin")
  public ResponseEntity<String> signin(
      @Valid @RequestBody SignInRequest signInRequest, HttpServletResponse response) {
    String token = securityService.login(signInRequest);
    setCookie(token, response);
    return ResponseEntity.ok("Successfully logged in");
  }

  void setCookie(String token, HttpServletResponse response) {
    Cookie cookie = new Cookie("token", token);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    response.addCookie(cookie);
  }
}
