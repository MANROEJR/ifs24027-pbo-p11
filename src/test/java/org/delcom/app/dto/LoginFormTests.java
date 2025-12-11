package org.delcom.app.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class LoginFormTest {

    @Test
    void testGettersAndSetters() {
        LoginForm loginForm = new LoginForm();
        
        loginForm.setEmail("test@test.com");
        loginForm.setPassword("password123");
        loginForm.setRememberMe(true);
        
        assertEquals("test@test.com", loginForm.getEmail());
        assertEquals("password123", loginForm.getPassword());
        assertTrue(loginForm.isRememberMe());
    }
}