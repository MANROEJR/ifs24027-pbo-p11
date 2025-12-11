package org.delcom.app.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.Mockito.*;

class RequestLoggingFilterTests {

    private RequestLoggingFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        // 1. Setup komponen utama sebelum setiap test
        filter = new RequestLoggingFilter();
        ReflectionTestUtils.setField(filter, "port", 8080);
        ReflectionTestUtils.setField(filter, "livereload", false);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        // 2. Setup default behavior
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 200, 404, 500})
    @DisplayName("Filter menampilkan log warna sesuai status code")
    void testLogColorsByStatus(int status) throws ServletException, IOException {
        // Arrange
        when(response.getStatus()).thenReturn(status);

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert: Pastikan request diteruskan ke filter berikutnya
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter tetap meneruskan request untuk URI /.well-known (Skip Log)")
    void testSkipWellKnown() throws ServletException, IOException {
        // Arrange: Override URI khusus
        when(request.getRequestURI()).thenReturn("/.well-known/acme-challenge");
        when(response.getStatus()).thenReturn(200);

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        verify(chain, times(1)).doFilter(request, response);
    }
}