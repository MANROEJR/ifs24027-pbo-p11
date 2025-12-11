package org.delcom.app.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class RegisterFormTest {

    @Test
    void testGettersAndSetters() {
        RegisterForm registerForm = new RegisterForm();
        
        registerForm.setName("User Test");
        registerForm.setEmail("user@test.com");
        registerForm.setPassword("pass123");
        
        assertEquals("User Test", registerForm.getName());
        assertEquals("user@test.com", registerForm.getEmail());
        assertEquals("pass123", registerForm.getPassword());
    }
}