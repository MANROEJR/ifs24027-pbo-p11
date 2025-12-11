package org.delcom.app.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthContextTests {

    @Test
    @DisplayName("Test kondisi awal (Belum ada user)")
    void testInitialState() {
        AuthContext authContext = new AuthContext();

        // Harusnya null dan false
        assertNull(authContext.getAuthUser());
        assertFalse(authContext.isAuthenticated());
    }

    @Test
    @DisplayName("Test set user (Login)")
    void testSetAuthUser() {
        AuthContext authContext = new AuthContext();
        User user = new User("Abdullah Ubaid", "test@example.com", "123456");

        // Set user
        authContext.setAuthUser(user);

        // Validasi
        assertEquals(user, authContext.getAuthUser());
        assertTrue(authContext.isAuthenticated());
    }

    @Test
    @DisplayName("Test set null (Logout/User Kosong)")
    void testSetUserNull() {
        AuthContext authContext = new AuthContext();
        User user = new User("Abdullah Ubaid", "test@example.com", "123456");
        
        // Login dulu
        authContext.setAuthUser(user);
        assertTrue(authContext.isAuthenticated());

        // Set null (Logout)
        authContext.setAuthUser(null);

        // Validasi
        assertNull(authContext.getAuthUser());
        assertFalse(authContext.isAuthenticated()); // Pakai assertFalse lebih bersih daripada assertTrue(!...)
    }
}