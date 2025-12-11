package org.delcom.app.interceptors;

import java.util.UUID;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthContext authContext;
    private final AuthTokenService authTokenService;
    private final UserService userService;

    // Gunakan Constructor Injection (Best Practice)
    public AuthInterceptor(AuthContext authContext, AuthTokenService authTokenService, UserService userService) {
        this.authContext = authContext;
        this.authTokenService = authTokenService;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        // 1. Lewati pengecekan untuk endpoint public
        if (isPublicEndpoint(request)) {
            return true;
        }

        // 2. Ambil bearer token
        String rawAuthToken = request.getHeader("Authorization");
        String token = extractToken(rawAuthToken);

        // 3. Validasi keberadaan token
        if (token == null || token.isEmpty()) {
            // Refactoring: Menghapus blok if (!isApiRequest) kosong yang menyebabkan missed branch
            sendErrorResponse(response, 401, "Token autentikasi tidak ditemukan");
            return false;
        }

        // 4. Validasi format token JWT
        if (!JwtUtil.validateToken(token, true)) {
            sendErrorResponse(response, 401, "Token autentikasi tidak valid");
            return false;
        }

        // 5. Ekstrak userId
        UUID userId = JwtUtil.extractUserId(token);
        if (userId == null) {
            sendErrorResponse(response, 401, "Format token autentikasi tidak valid");
            return false;
        }

        // 6. Cek DB Token
        AuthToken authToken = authTokenService.findUserToken(userId, token);
        if (authToken == null) {
            sendErrorResponse(response, 401, "Token autentikasi sudah expired");
            return false;
        }

        // 7. Cek User
        User authUser = userService.getUserById(authToken.getUserId());
        if (authUser == null) {
            sendErrorResponse(response, 404, "User tidak ditemukan");
            return false;
        }

        // 8. Set Context
        authContext.setAuthUser(authUser);
        return true;
    }

    // Ubah ke protected/package-private agar bisa ditest unit secara terisolasi jika perlu
    // Tapi private pun tidak masalah jika test lewat preHandle
    private String extractToken(String rawAuthToken) {
        if (rawAuthToken != null && rawAuthToken.startsWith("Bearer ")) {
            return rawAuthToken.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth") ||
               path.startsWith("/auth") ||
               path.equals("/error") ||
               path.startsWith("/uploads") ||
               path.startsWith("/css") ||
               path.startsWith("/js") ||
               path.startsWith("/images");
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format("{\"status\":\"fail\",\"message\":\"%s\",\"data\":null}", message);
        response.getWriter().write(jsonResponse);
    }
}