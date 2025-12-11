package org.delcom.app.interceptors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTests {

    @Mock private AuthContext authContext;
    @Mock private AuthTokenService authTokenService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        authInterceptor = new AuthInterceptor(authContext, authTokenService, userService);
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    // 1. Test Public Endpoints
    @Test
    void testPublicEndpoints() throws Exception {
        String[] publicPaths = {
            "/api/auth/login", "/auth/register", "/error", 
            "/uploads/img.jpg", "/css/style.css", "/js/app.js", "/images/logo.png"
        };

        for (String path : publicPaths) {
            when(request.getRequestURI()).thenReturn(path);
            boolean result = authInterceptor.preHandle(request, response, null);
            assertTrue(result, "Path " + path + " harusnya public");
        }
    }

    // 2. Token Null (Header Missing)
    @Test
    void testTokenMissing() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).setStatus(401);
    }

    // 3. [BARU] Token Empty String (Header ada "Bearer " tapi kosong isinya)
    // INI YANG AKAN MEMBUAT COVERAGE JADI 100%
    @Test
    void testTokenEmptyString() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        // Kasus: Header valid prefixnya, tapi isinya kosong string
        when(request.getHeader("Authorization")).thenReturn("Bearer "); 

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        // Memastikan masuk ke blok if (token == null || token.isEmpty())
        verify(response).setStatus(401); 
    }
    
    // 4. Token Format Salah (Tanpa 'Bearer ')
    @Test
    void testTokenMalformed() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Basic 12345");

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).setStatus(401);
    }

    // 5. JWT Invalid
    @Test
    void testJwtInvalid() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer token_palsu");

        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.validateToken("token_palsu", true)).thenReturn(false);

            boolean result = authInterceptor.preHandle(request, response, null);
            assertFalse(result);
            verify(response).setStatus(401);
        }
    }

    // 6. JWT Valid tapi UserID Null
    @Test
    void testJwtUserIdNull() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer token_valid_no_id");

        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.validateToken("token_valid_no_id", true)).thenReturn(true);
            jwtMock.when(() -> JwtUtil.extractUserId("token_valid_no_id")).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, null);
            assertFalse(result);
        }
    }

    // 7. Token Tidak Ditemukan di DB
    @Test
    void testTokenNotFoundInDB() throws Exception {
        UUID userId = UUID.randomUUID();
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer token_db_missing");

        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.validateToken(anyString(), eq(true))).thenReturn(true);
            jwtMock.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(userId);

            when(authTokenService.findUserToken(userId, "token_db_missing")).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, null);
            assertFalse(result);
        }
    }

    // 8. User Tidak Ditemukan di DB
    @Test
    void testUserNotFoundInDB() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthToken tokenEntity = new AuthToken(userId, "token_user_missing");

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer token_user_missing");

        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.validateToken(anyString(), eq(true))).thenReturn(true);
            jwtMock.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(userId);

            when(authTokenService.findUserToken(userId, "token_user_missing")).thenReturn(tokenEntity);
            when(userService.getUserById(userId)).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, null);
            assertFalse(result);
            verify(response).setStatus(404);
        }
    }

    // 9. Sukses
    @Test
    void testSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthToken tokenEntity = new AuthToken(userId, "token_valid");
        User user = new User();
        user.setId(userId);

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer token_valid");

        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.validateToken(anyString(), eq(true))).thenReturn(true);
            jwtMock.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(userId);

            when(authTokenService.findUserToken(userId, "token_valid")).thenReturn(tokenEntity);
            when(userService.getUserById(userId)).thenReturn(user);

            boolean result = authInterceptor.preHandle(request, response, null);
            
            assertTrue(result);
            verify(authContext).setAuthUser(user);
        }
    }
}