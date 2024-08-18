package com.taskmanagement.service;

import com.taskmanagement.dao.UserRepository;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.model.RoleList;
import com.taskmanagement.model.dto.SignInRequest;
import com.taskmanagement.model.dto.SignUpRequest;
import com.taskmanagement.model.entity.User;
import com.taskmanagement.security.JwtCore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private AuthenticationManager authenticationManager;

    @Mock private JwtCore jwtCore;

    @InjectMocks
    private SecurityService securityService;

    @Test
    void testRegister_NewUser_Success() {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username("username")
                .password("password")
                .email("email@email")
                .role(RoleList.AUTHOR) // добавьте роль, если это необходимо
                .build();

        // Настройка поведения моков
        when(userRepository.existsUserByUsername(signUpRequest.getUsername())).thenReturn(false);
        when(userRepository.existsUserByEmail(signUpRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signUpRequest.getPassword())).thenReturn("encodedPassword");

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .password("encodedPassword")
                .email(signUpRequest.getEmail())
                .role(signUpRequest.getRole())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                signUpRequest.getUsername(), signUpRequest.getPassword());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtCore.generateToken(any(Authentication.class))).thenReturn("jwtToken");

        String token = securityService.register(signUpRequest);

        assertEquals("jwtToken", token);

        verify(userRepository, times(1)).save(any(User.class));
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtCore, times(1)).generateToken(any(Authentication.class));
    }

    @Test
    void testRegister_ExistingUsername_ExceptionThrown() {
        SignUpRequest signUpRequest =
                SignUpRequest.builder()
                        .username("username")
                        .password("password")
                        .build();
        when(userRepository.existsUserByUsername(signUpRequest.getUsername())).thenReturn(true);

        assertThrows(
                UnauthorizedException.class,
                () -> {
                    securityService.register(signUpRequest);
                });
        verify(userRepository, never()).save(any());
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtCore, never()).generateToken(any());
    }
    @Test
    void testRegister_ExistingEmail_ExceptionThrown() {
        SignUpRequest signUpRequest =
                SignUpRequest.builder()
                        .username("username")
                        .password("password")
                        .email("email@email")
                        .build();
        when(userRepository.existsUserByEmail(signUpRequest.getEmail())).thenReturn(true);

        assertThrows(
                UnauthorizedException.class,
                () -> {
                    securityService.register(signUpRequest);
                });
        verify(userRepository, never()).save(any());
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtCore, never()).generateToken(any());
    }



    @Test
    public void testLogin_IncorrectCredentials() {
        // Arrange
        String email = "user@example.com";
        String password = "password";
        SignInRequest signInRequest = SignInRequest.builder()
                .email(email)
                .password(password)
                .build();


        when(userRepository.findUsersByEmail(email))
                .thenThrow(new UnauthorizedException("Incorrect credentials"));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> securityService.login(signInRequest));
        verify(userRepository).findUsersByEmail(email);
    }
    @Test
    public void testLogin_Success() {
        // Arrange
        String email = "user@example.com";
        String password = "password";
        SignInRequest signInRequest = SignInRequest.builder()
                .email(email)
                .password(password)
                .build();

        User user = new User();
        user.setUsername("username");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findUsersByEmail(email)).thenReturn(Optional.of(user));
        when(jwtCore.generateToken(authentication)).thenReturn("token");

        // Act
        String token = securityService.login(signInRequest);

        // Assert
        assertEquals("token", token);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findUsersByEmail(email);
        verify(jwtCore).generateToken(authentication);
    }
    @Test
    public void testLogin_BadCredentialsException() {
        // Arrange
        String email = "user@example.com";
        String password = "password";
        SignInRequest signInRequest =  SignInRequest.builder()
                .email(email)
                .password(password)
                .build();

        User user = new User();
        user.setUsername("username");

        when(userRepository.findUsersByEmail(email)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> securityService.login(signInRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findUsersByEmail(email);
    }

}
