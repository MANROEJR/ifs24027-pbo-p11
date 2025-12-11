package org.delcom.app.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class) // 1. Gunakan ini agar lebih ringan (tanpa SpringBootTest)
class CustomErrorControllerTest {

    @Mock
    private ErrorAttributes errorAttributes; // Mock object otomatis

    @InjectMocks
    private CustomErrorController controller; // Inject mock ke controller otomatis

    @Test
    @DisplayName("Mengembalikan response error default 500")
    void testHandleErrorReturns500() {
        // Setup: ErrorAttributes mengembalikan map kosong (default behavior)
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(Map.of());

        // Buat dummy request sederhana
        ServletWebRequest webRequest = new ServletWebRequest(mock(HttpServletRequest.class));

        // Eksekusi
        ResponseEntity<Map<String, Object>> result = controller.handleError(webRequest);

        // Validasi
        assertEquals(500, result.getStatusCode().value());
        assertEquals("error", result.getBody().get("status"));
        assertEquals("Unknown Error", result.getBody().get("error"));
    }

    @Test
    @DisplayName("Mengembalikan response error 404")
    void testHandleErrorReturns404() {
        // Setup: ErrorAttributes mengembalikan data 404
        Map<String, Object> errorMap = Map.of(
                "status", 404,
                "error", "Not Found",
                "path", "/api/salah");

        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(errorMap);

        // Buat dummy request sederhana
        ServletWebRequest webRequest = new ServletWebRequest(mock(HttpServletRequest.class));

        // Eksekusi
        ResponseEntity<Map<String, Object>> result = controller.handleError(webRequest);

        // Validasi
        assertEquals(404, result.getStatusCode().value());
        assertEquals("fail", result.getBody().get("status")); // 4xx dianggap 'fail'
        assertEquals("Not Found", result.getBody().get("error"));
        assertEquals("/api/salah", result.getBody().get("path"));
    }
}