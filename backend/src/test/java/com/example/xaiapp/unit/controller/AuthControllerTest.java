/**
 * Unit tests for AuthController
 * Tests authentication endpoints with mocked services
 */
package com.example.xaiapp.unit.controller;

import com.example.xaiapp.controller.AuthController;
import com.example.xaiapp.dto.ApiResponse;
import com.example.xaiapp.dto.JwtAuthResponse;
import com.example.xaiapp.dto.LoginRequest;
import com.example.xaiapp.dto.UserDto;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthController authController;

    private UserDto testUserDto;
    private User testUser;
    private LoginRequest loginRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        authentication = mock(Authentication.class);
    }

    /**
     * Test successful user registration
     * Verifies that valid registration returns 200 OK with success message
     */
    @Test
    void testRegister_Success() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        ResponseEntity<ApiResponse> response = authController.register(testUserDto);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("User registered successfully!", response.getBody().getMessage());
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test registration with duplicate username
     * Verifies that duplicate username returns 400 Bad Request
     */
    @Test
    void testRegister_UsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        ResponseEntity<ApiResponse> response = authController.register(testUserDto);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Username is already taken!", response.getBody().getMessage());
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test registration with duplicate email
     * Verifies that duplicate email returns 400 Bad Request
     */
    @Test
    void testRegister_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        ResponseEntity<ApiResponse> response = authController.register(testUserDto);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Email is already in use!", response.getBody().getMessage());
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test registration with exception
     * Verifies that registration errors return 400 Bad Request
     */
    @Test
    void testRegister_Exception() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<ApiResponse> response = authController.register(testUserDto);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Error occurred during registration"));
    }

    /**
     * Test successful user login
     * Verifies that valid credentials return 200 OK with JWT token
     */
    @Test
    void testLogin_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof JwtAuthResponse);
        JwtAuthResponse jwtResponse = (JwtAuthResponse) response.getBody();
        assertEquals("jwt-token", jwtResponse.getAccessToken());
        assertEquals(1L, jwtResponse.getUserId());
        assertEquals("testuser", jwtResponse.getUsername());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, times(1)).generateToken(authentication);
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    /**
     * Test login with invalid credentials
     * Verifies that invalid credentials return 400 Bad Request
     */
    @Test
    void testLogin_BadCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Invalid username or password", apiResponse.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, never()).generateToken(any());
        verify(userRepository, never()).findByUsername(anyString());
    }

    /**
     * Test login with non-existent user
     * Verifies that missing user returns 400 Bad Request
     */
    @Test
    void testLogin_UserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Invalid username or password", apiResponse.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, times(1)).generateToken(authentication);
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    /**
     * Test login with exception
     * Verifies that login errors return 400 Bad Request
     */
    @Test
    void testLogin_Exception() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Invalid username or password", apiResponse.getMessage());
    }
}

