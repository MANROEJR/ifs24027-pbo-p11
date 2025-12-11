package org.delcom.app.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserTests {

    @Test
    @DisplayName("Membuat instance dari kelas User")
    void testMembuatInstanceUser() {
        
        // 1. User dengan nama, email, dan password (Constructor 3 parameter)
        {
            User user = new User("Name", "email@example.com", "password123");

            assertEquals("Name", user.getName());
            assertEquals("email@example.com", user.getEmail());
            assertEquals("password123", user.getPassword());
        }

        // 2. User dengan email dan password (Constructor 2 parameter)
        // Note: Di User.java, constructor ini memanggil this("", email, password)
        // Jadi name diharapkan string kosong "", bukan null.
        {
            User user = new User("email@example.com", "password123");
            
            assertEquals("", user.getName()); 
            assertEquals("email@example.com", user.getEmail());
            assertEquals("password123", user.getPassword());
        }

        // 3. User dengan nilai default (Constructor kosong)
        {
            User user = new User();

            assertNull(user.getId());
            assertNull(user.getName());
            assertNull(user.getEmail());
            assertNull(user.getPassword());
        }

        // 4. User dengan Setter dan Lifecycle Methods (onCreate, onUpdate)
        {
            User user = new User();
            UUID generatedId = UUID.randomUUID();
            
            user.setId(generatedId);
            user.setName("Set Name");
            user.setEmail("Set Email");
            user.setPassword("Set Password");
            
            // Simulasikan event JPA @PrePersist dan @PreUpdate
            user.onCreate();
            user.onUpdate();

            assertEquals(generatedId, user.getId());
            assertEquals("Set Name", user.getName());
            assertEquals("Set Email", user.getEmail());
            assertEquals("Set Password", user.getPassword());
            
            // Gunakan assertNotNull untuk mengecek objek tidak null
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
        }
    }
}