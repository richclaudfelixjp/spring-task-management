package com.example.taskmanagement.service;

import com.example.taskmanagement.model.User;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.taskmanagement.repository.UserRepository;

public class UserServiceTest {
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        
        Mockito.when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WhenUserNotFound_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String username = "nonexistentuser";
        Mockito.when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username)
        );
        
        assertEquals("User not found with username: " + username, exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetailsWithEmptyAuthorities() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        
        Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails.getAuthorities());
        assertEquals(0, userDetails.getAuthorities().size());
    }
}

