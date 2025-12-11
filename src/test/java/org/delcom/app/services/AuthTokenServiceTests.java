package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // Mengaktifkan fitur anotasi Mockito
class AuthTokenServiceTests {

    @Mock
    private AuthTokenRepository authTokenRepository; // Mock Repository otomatis

    @InjectMocks
    private AuthTokenService authTokenService; // Inject Mock ke Service otomatis

    @Test
    @DisplayName("Test Create AuthToken (Save)")
    void testCreateAuthToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        AuthToken token = new AuthToken(userId, "token-123");
        
        when(authTokenRepository.save(any(AuthToken.class))).thenReturn(token);

        // Act
        AuthToken result = authTokenService.createAuthToken(token);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("token-123", result.getToken());
    }

    @Test
    @DisplayName("Test Find User Token")
    void testFindUserToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String tokenStr = "token-cari";
        AuthToken token = new AuthToken(userId, tokenStr);

        when(authTokenRepository.findUserToken(userId, tokenStr)).thenReturn(token);

        // Act
        AuthToken result = authTokenService.findUserToken(userId, tokenStr);

        // Assert
        assertNotNull(result);
        assertEquals(tokenStr, result.getToken());
    }

    @Test
    @DisplayName("Test Delete AuthToken")
    void testDeleteAuthToken() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        authTokenService.deleteAuthToken(userId);

        // Assert: Verifikasi bahwa repository dipanggil 1 kali
        verify(authTokenRepository, times(1)).deleteByUserId(userId);
    }
}