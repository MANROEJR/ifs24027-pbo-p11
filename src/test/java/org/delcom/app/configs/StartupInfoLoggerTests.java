package org.delcom.app.configs;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment; // <-- IMPORT YANG BENAR

@ExtendWith(MockitoExtension.class)
class StartupInfoLoggerTests {

    private StartupInfoLogger logger;
    private ApplicationReadyEvent event;
    private ConfigurableApplicationContext context;
    
    // UBAH TIPE INI DARI Environment KE ConfigurableEnvironment
    private ConfigurableEnvironment env; 

    @BeforeEach
    void setUp() {
        logger = new StartupInfoLogger();
        event = mock(ApplicationReadyEvent.class);
        context = mock(ConfigurableApplicationContext.class);
        
        // MOCK CLASS YANG LEBIH SPESIFIK
        env = mock(ConfigurableEnvironment.class); 
    }

    // Helper method untuk setup mock environment standar
    // Dipanggil hanya oleh test yang membutuhkan context valid
    private void setupStandardMocks() {
        // Sekarang ini valid karena tipe env sudah ConfigurableEnvironment
        when(event.getApplicationContext()).thenReturn(context);
        when(context.getEnvironment()).thenReturn(env);

        // Mock properti dasar
        when(env.getProperty("server.port", "8080")).thenReturn("8080");
        when(env.getProperty("server.address", "localhost")).thenReturn("localhost");
        when(env.getProperty("spring.devtools.livereload.port", "35729")).thenReturn("35729");
    }

    @Test
    @DisplayName("Skenario 1: ContextPath NULL & LiveReload DISABLED")
    void testLog_NullContext_LiveReloadFalse() {
        setupStandardMocks();

        // Context path NULL
        when(env.getProperty("server.servlet.context-path", "/")).thenReturn(null);
        
        // LiveReload FALSE
        when(env.getProperty(eq("spring.devtools.livereload.enabled"), eq(Boolean.class), eq(false)))
            .thenReturn(false);

        logger.onApplicationEvent(event);
    }

    @Test
    @DisplayName("Skenario 2: ContextPath ROOT ('/') & LiveReload ENABLED")
    void testLog_RootContext_LiveReloadTrue() {
        setupStandardMocks();

        // Context path "/"
        when(env.getProperty("server.servlet.context-path", "/")).thenReturn("/");
        
        // LiveReload TRUE
        when(env.getProperty(eq("spring.devtools.livereload.enabled"), eq(Boolean.class), eq(false)))
            .thenReturn(true);

        logger.onApplicationEvent(event);
    }

    @Test
    @DisplayName("Skenario 3: ContextPath CUSTOM ('/app')")
    void testLog_CustomContext() {
        setupStandardMocks();

        // Context path "/app"
        when(env.getProperty("server.servlet.context-path", "/")).thenReturn("/app");
        
        // LiveReload FALSE
        when(env.getProperty(eq("spring.devtools.livereload.enabled"), eq(Boolean.class), eq(false)))
            .thenReturn(false);

        logger.onApplicationEvent(event);
    }
    
    @Test
    @DisplayName("Skenario 4: Guard Clause (Context Null)")
    void testContextNullSafety() {
        // Tidak panggil setupStandardMocks() agar tidak ada Unnecessary Stubbing
        when(event.getApplicationContext()).thenReturn(null);
        
        logger.onApplicationEvent(event);
    }
}