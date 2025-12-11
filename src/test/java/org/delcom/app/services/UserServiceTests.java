package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // 1. Create User
    @Test
    void testCreateUser() {
        User user = new User("Name", "email@test.com", "pass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser("Name", "email@test.com", "pass");

        assertNotNull(result);
        assertEquals("Name", result.getName());
        verify(userRepository).save(any(User.class));
    }

    // 2. Get By Email
    @Test
    void testGetUserByEmail_Found() {
        User user = new User();
        when(userRepository.findFirstByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("test@test.com");
        assertNotNull(result);
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findFirstByEmail("test@test.com")).thenReturn(Optional.empty());

        User result = userService.getUserByEmail("test@test.com");
        assertNull(result);
    }

    // 3. Get By ID
    @Test
    void testGetUserById_Found() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(new User()));

        User result = userService.getUserById(id);
        assertNotNull(result);
    }

    @Test
    void testGetUserById_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.getUserById(id);
        assertNull(result);
    }

    // 4. Update User
    @Test
    @DisplayName("Update User: Found -> Success")
    void testUpdateUser_Found() {
        UUID id = UUID.randomUUID();
        User existing = new User("Old", "old@mail.com", "pass");
        
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userService.updateUser(id, "New", "new@mail.com");

        assertNotNull(result);
        assertEquals("New", result.getName());
        assertEquals("new@mail.com", result.getEmail());
    }

    @Test
    @DisplayName("Update User: Not Found -> Return Null")
    void testUpdateUser_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.updateUser(id, "New", "new@mail.com");
        assertNull(result);
    }

    // 5. Update Password
    @Test
    @DisplayName("Update Password: Found -> Success")
    void testUpdatePassword_Found() {
        UUID id = UUID.randomUUID();
        User existing = new User("Name", "mail", "OldPass");

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userService.updatePassword(id, "NewPass");

        assertNotNull(result);
        assertEquals("NewPass", result.getPassword());
    }

    @Test
    @DisplayName("Update Password: Not Found -> Return Null")
    void testUpdatePassword_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.updatePassword(id, "NewPass");
        assertNull(result);
    }
}