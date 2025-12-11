package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebMvcConfigTest {

    @Mock
    private AuthInterceptor authInterceptor;

    @Mock
    private ResourceHandlerRegistry resourceHandlerRegistry;

    @Mock
    private ResourceHandlerRegistration resourceHandlerRegistration;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    @InjectMocks
    private WebMvcConfig webMvcConfig;

    @Test
    void testAddResourceHandlers() {
        // Setup: Ketika addResourceHandler dipanggil, kembalikan objek registration palsu (mock)
        // ini diperlukan karena methodnya berantai (chaining method)
        when(resourceHandlerRegistry.addResourceHandler(anyString()))
                .thenReturn(resourceHandlerRegistration);
        when(resourceHandlerRegistration.addResourceLocations(anyString()))
                .thenReturn(resourceHandlerRegistration);

        // Action: Jalankan method yang mau ditest
        webMvcConfig.addResourceHandlers(resourceHandlerRegistry);

        // Verify: Pastikan konfigurasi uploads didaftarkan
        verify(resourceHandlerRegistry).addResourceHandler("/uploads/**");
        verify(resourceHandlerRegistration).addResourceLocations("file:./uploads/");
        
        // Verify: Pastikan static files default juga didaftarkan
        verify(resourceHandlerRegistry).addResourceHandler("/**");
        verify(resourceHandlerRegistration).addResourceLocations("classpath:/static/");
    }

    @Test
    void testAddInterceptors() {
        // Setup: Mocking untuk chaining method interceptor
        when(interceptorRegistry.addInterceptor(authInterceptor))
                .thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(anyString()))
                .thenReturn(interceptorRegistration);
        when(interceptorRegistration.excludePathPatterns(anyString()))
                .thenReturn(interceptorRegistration);

        // Action
        webMvcConfig.addInterceptors(interceptorRegistry);

        // Verify: Pastikan interceptor didaftarkan
        verify(interceptorRegistry).addInterceptor(authInterceptor);

        // Verify: Pastikan path pattern sesuai
        verify(interceptorRegistration).addPathPatterns("/api/**");

        // Verify: Pastikan pengecualian path (exclude) sesuai
        verify(interceptorRegistration).excludePathPatterns("/api/auth/**");
        verify(interceptorRegistration).excludePathPatterns("/uploads/**");
    }
}