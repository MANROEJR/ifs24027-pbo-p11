package org.delcom.app.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthTokenTests {

    @Test
    @DisplayName("Membuat instance dari kelas AuthToken")
    void testMembuatInstanceAuthToken() {
        
        // 1. Tes Constructor dengan Parameter (userId, token)
        // Constructor ini secara otomatis men-set createdAt = LocalDateTime.now()
        {
            UUID userId = UUID.randomUUID();
            String tokenString = "token123";
            
            AuthToken authToken = new AuthToken(userId, tokenString);

            assertEquals(tokenString, authToken.getToken());
            assertEquals(userId, authToken.getUserId());
            
            // Verifikasi bahwa createdAt terisi otomatis oleh constructor
            assertNotNull(authToken.getCreatedAt(), "createdAt harusnya tidak null saat menggunakan constructor berparameter");
        }

        // 2. Tes Constructor Default (Tanpa Parameter)
        // Constructor ini tidak men-set createdAt
        {
            AuthToken authToken = new AuthToken();

            assertNull(authToken.getId());
            assertNull(authToken.getToken());
            assertNull(authToken.getUserId());
            assertNull(authToken.getCreatedAt());
        }

        // 3. Tes Setters dan Method onCreate (@PrePersist)
        {
            AuthToken authToken = new AuthToken();
            UUID generatedId = UUID.randomUUID();
            UUID generatedUserId = UUID.randomUUID();
            String tokenString = "Set Token";

            authToken.setId(generatedId);
            authToken.setUserId(generatedUserId);
            authToken.setToken(tokenString);
            
            // Simulasikan event @PrePersist dengan memanggil onCreate secara manual
            // Method onCreate bersifat protected, namun bisa diakses karena test berada di package yang sama
            authToken.onCreate();

            assertEquals(generatedId, authToken.getId());
            assertEquals(generatedUserId, authToken.getUserId());
            assertEquals(tokenString, authToken.getToken());
            
            // Verifikasi bahwa onCreate berhasil mengisi createdAt
            assertNotNull(authToken.getCreatedAt(), "createdAt harusnya terisi setelah memanggil onCreate()");
        }
    }
}