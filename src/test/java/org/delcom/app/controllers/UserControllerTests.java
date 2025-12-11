package org.delcom.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserControllerTests {

    @Test
    @DisplayName("Pengujian UserController dengan berbagai skenario")
    public void testVariousUserController() {

        // 1. Setup Mock Services
        AuthTokenService authTokenService = Mockito.mock(AuthTokenService.class);
        UserService userService = Mockito.mock(UserService.class);

        // 2. Init Controller & AuthContext
        UserController userController = new UserController(userService, authTokenService);
        userController.authContext = new AuthContext();

        // ==========================================
        // TEST: METHOD registerUser
        // ==========================================
        {
            // Skenario: Data tidak valid (Null/Empty)
            {
                List<User> invalidUsers = List.of(
                        new User(null, "email@example.com", "password123"), // Nama Null
                        new User("", "email@example.com", "password123"),   // Nama Kosong
                        new User("User", null, "password123"),              // Email Null
                        new User("User", "", "password123"),                // Email Kosong
                        new User("User", "email@example.com", null),        // Password Null
                        new User("User", "email@example.com", "")           // Password Kosong
                );

                for (User user : invalidUsers) {
                    ResponseEntity<ApiResponse<Map<String, UUID>>> result = userController.registerUser(user);
                    assertNotNull(result);
                    assertTrue(result.getStatusCode().is4xxClientError());
                    assertEquals("fail", result.getBody().getStatus());
                }
            }

            // Skenario: Email sudah terdaftar
            {
                User existingUser = new User("Existing User", "existing@example.com", "password123");
                Mockito.when(userService.getUserByEmail("existing@example.com")).thenReturn(existingUser);

                ResponseEntity<ApiResponse<Map<String, UUID>>> result = userController.registerUser(existingUser);
                assertNotNull(result);
                assertTrue(result.getStatusCode().is4xxClientError());
                assertEquals("fail", result.getBody().getStatus());
            }

            // Skenario: Registrasi sukses
            {
                User newUser = new User("New User", "new@example.com", "password123");
                newUser.setId(UUID.randomUUID());

                Mockito.when(userService.getUserByEmail("new@example.com")).thenReturn(null);
                Mockito.when(userService.createUser(anyString(), anyString(), anyString()))
                        .thenReturn(newUser);

                ResponseEntity<ApiResponse<Map<String, UUID>>> result = userController.registerUser(newUser);
                assertNotNull(result);
                assertTrue(result.getStatusCode().is2xxSuccessful());
                assertEquals("success", result.getBody().getStatus());
            }
        }

        // ==========================================
        // TEST: METHOD loginUser
        // ==========================================
        {
            // Skenario: Data tidak valid
            {
                List<User> invalidUsers = List.of(
                        new User(null, "password123"),       // Email Null
                        new User("", "password123"),         // Email Kosong
                        new User("user@example.com", null),  // Password Null
                        new User("user@example.com", "")     // Password Kosong
                );

                for (User user : invalidUsers) {
                    ResponseEntity<ApiResponse<Map<String, String>>> result = userController.loginUser(user);
                    assertNotNull(result);
                    assertTrue(result.getStatusCode().is4xxClientError());
                    assertEquals("fail", result.getBody().getStatus());
                }
            }

            // Skenario: Email atau password salah
            {
                String password = "password123";
                String hashedPassword = new BCryptPasswordEncoder().encode(password);
                UUID userId = UUID.randomUUID();

                User fakeUser = new User("Fake User", "user@example.com", hashedPassword);
                fakeUser.setId(userId);

                // Case: User tidak ditemukan (Email salah)
                Mockito.when(userService.getUserByEmail("user@example.com")).thenReturn(null);
                ResponseEntity<ApiResponse<Map<String, String>>> result = userController.loginUser(fakeUser);
                assertNotNull(result);
                assertTrue(result.getStatusCode().is4xxClientError());
                assertEquals("fail", result.getBody().getStatus());

                // Case: Password salah
                Mockito.when(userService.getUserByEmail("user@example.com")).thenReturn(fakeUser);
                ResponseEntity<ApiResponse<Map<String, String>>> result2 = userController
                        .loginUser(new User("user@example.com", "wrongpassword"));
                assertNotNull(result2);
                assertTrue(result2.getStatusCode().is4xxClientError());
                assertEquals("fail", result2.getBody().getStatus());
            }

            // Skenario: Gagal membuat auth token (Server Error)
            {
                String password = "password123";
                String hashedPassword = new BCryptPasswordEncoder().encode(password);
                UUID userId = UUID.randomUUID();

                // Kita generate token dummy untuk mock response, 
                // tapi di controller token digenerate internal
                String bearerToken = JwtUtil.generateToken(userId);
                AuthToken fakeAuthToken = new AuthToken(userId, bearerToken);

                User fakeReqUser = new User("Fake User", "user@example.com", password);
                fakeReqUser.setId(userId);

                User fakeUser = new User("Fake User", "user@example.com", hashedPassword);
                fakeUser.setId(userId);

                Mockito.when(userService.getUserByEmail("user@example.com")).thenReturn(fakeUser);

                // Case: Ada token lama, tapi gagal create token baru
                {
                    // Mock findUserToken dengan anyString() karena controller generate token baru
                    Mockito.when(authTokenService.findUserToken(eq(userId), anyString()))
                            .thenReturn(fakeAuthToken);
                    
                    Mockito.doNothing().when(authTokenService).deleteAuthToken(eq(userId));
                    
                    // Simulasi gagal simpan token baru
                    Mockito.when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(null);

                    ResponseEntity<ApiResponse<Map<String, String>>> result = userController.loginUser(fakeReqUser);
                    assertNotNull(result);
                    assertTrue(result.getStatusCode().is5xxServerError());
                    assertEquals("error", result.getBody().getStatus());
                }

                // Case: Tidak ada token lama, tapi gagal create token baru
                {
                    Mockito.when(authTokenService.findUserToken(eq(userId), anyString()))
                            .thenReturn(null);

                    Mockito.when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(null);

                    ResponseEntity<ApiResponse<Map<String, String>>> result = userController.loginUser(fakeReqUser);
                    assertNotNull(result);
                    assertTrue(result.getStatusCode().is5xxServerError());
                    assertEquals("error", result.getBody().getStatus());
                }

                // Case: Berhasil Login
                {
                    Mockito.when(authTokenService.findUserToken(eq(userId), anyString()))
                            .thenReturn(null);

                    Mockito.when(authTokenService.createAuthToken(any(AuthToken.class)))
                            .thenReturn(fakeAuthToken);

                    ResponseEntity<ApiResponse<Map<String, String>>> result = userController.loginUser(fakeReqUser);
                    assertNotNull(result);
                    assertTrue(result.getStatusCode().is2xxSuccessful());
                    assertEquals("success", result.getBody().getStatus());
                }
            }
        }

        // Setup Auth User untuk test berikutnya
        User authUser = new User("Auth User", "user@example.com", "password123");
        authUser.setId(UUID.randomUUID());

        // ==========================================
        // TEST: METHOD getUserInfo
        // ==========================================
        {
            // Case: Tidak terautentikasi
            {
                userController.authContext.setAuthUser(null);
                ResponseEntity<ApiResponse<Map<String, User>>> result = userController.getUserInfo();
                assertNotNull(result);
                assertTrue(result.getStatusCode().is4xxClientError());
                assertEquals("fail", result.getBody().getStatus());
            }

            // Case: Berhasil mendapatkan info user
            {
                userController.authContext.setAuthUser(authUser);
                ResponseEntity<ApiResponse<Map<String, User>>> result = userController.getUserInfo();
                assertNotNull(result);
                assertTrue(result.getStatusCode().is2xxSuccessful());
                assertEquals("success", result.getBody().getStatus());
            }
        }

        // ==========================================
        // TEST: METHOD updateUser
        // ==========================================
        {
            // Case: Tidak terautentikasi
            {
                userController.authContext.setAuthUser(null);
                ResponseEntity<ApiResponse<User>> result = userController.updateUser(authUser);
                assertNotNull(result);
                assertTrue(result.getStatusCode().is4xxClientError());
                assertEquals("fail", result.getBody().getStatus());
            }

            // Case: Data tidak valid
            {
                userController.authContext.setAuthUser(authUser);

                List<User> invalidUsers = List.of(
                        new User(null, "user@example.com", ""), // Nama Null
                        new User("", "user@example.com", ""),   // Nama Kosong
                        new User("Auth User", null, ""),        // Email Null
                        new User("Auth User", "", "")           // Email Kosong
                );

                for (User reqUser : invalidUsers) {
                    ResponseEntity<ApiResponse<User>> result = userController.updateUser(reqUser);
                    assertNotNull(result);
                    assertTrue(result.getStatusCode().is4xxClientError());
                    assertEquals("fail", result.getBody().getStatus());
                }
            }

            // Case: Gagal update user (User tidak ditemukan di DB)
            {
                Mockito.when(userService.updateUser(any(UUID.class), anyString(), anyString()))
                        .thenReturn(null);

                ResponseEntity<ApiResponse<User>> result = userController.updateUser(authUser);
                assertNotNull(result);
                assertTrue(result.getStatusCode().is4xxClientError());
                assertEquals("fail", result.getBody().getStatus());
            }

            // Case: Berhasil mengupdate user
            {
                Mockito.when(userService.updateUser(any(UUID.class), anyString(), anyString()))
                        .thenReturn(authUser);

                ResponseEntity<ApiResponse<User>> result = userController.updateUser(authUser);
                assertNotNull(result);
                assertTrue(result.getStatusCode().is2xxSuccessful());
                assertEquals("success", result.getBody().getStatus());
            }
        }

        // ==========================================
        // TEST: METHOD updateUserPassword
        // ==========================================
        {
            Map<String, String> passwordPayload = Map.of(
                    "password", "oldpassword123",
                    "newPassword", "newpassword123");

            // Case: Tidak terautentikasi
            {
                userController.authContext.setAuthUser(null);
                ResponseEntity<ApiResponse<Void>> result = userController.updateUserPassword(passwordPayload);
                assertNotNull(result);
                assertTrue(result.getStatusCode().is4xxClientError());
                assertEquals("fail", result.getBody().getStatus());
            }

            userController.authContext.setAuthUser(authUser);

            // Case: Data tidak valid (Payload kosong/null)
            {
                List<Map<String, String>> invalidPayloads = List.of(
                        Map.of("no-password", "", "newPassword", "newpassword123"), // Key salah
                        Map.of("password", "", "newPassword", "newpassword123"),    // Value kosong
                        Map.of("password", "oldpassword123", "no-newPassword", ""), // Key salah
                        Map.of("password", "oldpassword123", "newPassword", "")     // Value kosong
                );

                for (Map<String, String> payload : invalidPayloads) {
                    ResponseEntity<ApiResponse<Void>> result = userController.updateUserPassword(payload);
                    assertNotNull(result);
                    assertTrue(result.getStatusCode().is4xxClientError());
                    assertEquals("fail", result.getBody().getStatus());
                }
            }

            // Case: Password lama salah
            {
                // Set password asli di DB
                authUser.setPassword(new BCryptPasswordEncoder().encode("correctOldPassword"));
                
                // Input payload password lama: "oldpassword123" (salah)
                ResponseEntity<ApiResponse<Void>> result = userController.updateUserPassword(passwordPayload);
                assertNotNull(result);
                assertTrue(result.getStatusCode().is4xxClientError());
                assertEquals("fail", result.getBody().getStatus());
            }

            // Case: User tidak ditemukan saat update
            {
                authUser.setPassword(new BCryptPasswordEncoder().encode("oldpassword123"));

                Mockito.when(userService.updatePassword(any(UUID.class), anyString()))
                        .thenReturn(null);

                ResponseEntity<ApiResponse<Void>> result = userController.updateUserPassword(passwordPayload);
                assertNotNull(result);
                assertTrue(result.getStatusCode().is4xxClientError());
                assertEquals("fail", result.getBody().getStatus());
            }

            // Case: Berhasil mengupdate password
            {
                authUser.setPassword(new BCryptPasswordEncoder().encode("oldpassword123"));

                Mockito.when(userService.updatePassword(any(UUID.class), anyString()))
                        .thenReturn(authUser);
                
                // Pastikan delete token dipanggil
                Mockito.doNothing().when(authTokenService).deleteAuthToken(any(UUID.class));

                ResponseEntity<ApiResponse<Void>> result = userController.updateUserPassword(passwordPayload);
                assertNotNull(result);
                assertTrue(result.getStatusCode().is2xxSuccessful());
                assertEquals("success", result.getBody().getStatus());
            }
        }
    }
}