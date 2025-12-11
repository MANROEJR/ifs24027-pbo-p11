package org.delcom.app.configs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void testPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSecurityFilterChain() throws Exception {
        // 1. Mock HttpSecurity
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_SELF);

        // 2. Jalankan method utama
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(http);
        // assertNotNull(filterChain); // http.build() di-mock return null, flow code sudah jalan

        // =================================================================
        // TEKNIK CAPTURE LAMBDA
        // =================================================================

        // --- A. Test Exception Handling ---
        ArgumentCaptor<Customizer> exCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(http).exceptionHandling(exCaptor.capture());

        ExceptionHandlingConfigurer<HttpSecurity> exConfig = mock(ExceptionHandlingConfigurer.class);
        exCaptor.getValue().customize(exConfig);

        ArgumentCaptor<AuthenticationEntryPoint> entryPointCaptor = ArgumentCaptor.forClass(AuthenticationEntryPoint.class);
        verify(exConfig).authenticationEntryPoint(entryPointCaptor.capture());

        HttpServletResponse response = mock(HttpServletResponse.class);
        entryPointCaptor.getValue().commence(mock(HttpServletRequest.class), response, null);
        verify(response).sendRedirect("/auth/login");

        // --- B. Test Authorize Requests (BAGIAN YANG ERROR SEBELUMNYA) ---
        ArgumentCaptor<Customizer> authCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(http).authorizeHttpRequests(authCaptor.capture());

        // 1. Mock Registry
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authRegistry = 
            mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);

        // 2. Mock AuthorizedUrl (objek transisi)
        AuthorizeHttpRequestsConfigurer.AuthorizedUrl authorizedUrl = 
            mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);

        // 3. Konfigurasi Chaining:
        // Saat requestMatchers(...) dipanggil -> return authorizedUrl (Mocked)
        when(authRegistry.requestMatchers(any(String[].class))).thenReturn(authorizedUrl);
        
        // Saat authorizedUrl.permitAll() dipanggil -> return authRegistry kembali
        when(authorizedUrl.permitAll()).thenReturn(authRegistry);

        // Saat anyRequest() dipanggil -> return authorizedUrl
        when(authRegistry.anyRequest()).thenReturn(authorizedUrl);

        // Saat authorizedUrl.authenticated() dipanggil -> return authRegistry
        when(authorizedUrl.authenticated()).thenReturn(authRegistry);
        
        // 4. Jalankan lambda
        authCaptor.getValue().customize(authRegistry);

        // 5. Verifikasi
        verify(authRegistry).requestMatchers("/auth/**", "/assets/**", "/api/**", "/css/**", "/js/**");
        verify(authorizedUrl).permitAll();
        verify(authRegistry).anyRequest();
        verify(authorizedUrl).authenticated();


        // --- C. Test Form Login ---
        ArgumentCaptor<Customizer> formCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(http).formLogin(formCaptor.capture());
        
        FormLoginConfigurer<HttpSecurity> formConfig = mock(FormLoginConfigurer.class);
        formCaptor.getValue().customize(formConfig);
        verify(formConfig).disable();

        // --- D. Test Logout ---
        ArgumentCaptor<Customizer> logoutCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(http).logout(logoutCaptor.capture());

        LogoutConfigurer<HttpSecurity> logoutConfig = mock(LogoutConfigurer.class, RETURNS_SELF);
        logoutCaptor.getValue().customize(logoutConfig);
        verify(logoutConfig).logoutSuccessUrl("/auth/login");

        // --- E. Test Remember Me ---
        ArgumentCaptor<Customizer> rememberCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(http).rememberMe(rememberCaptor.capture());

        RememberMeConfigurer<HttpSecurity> rememberConfig = mock(RememberMeConfigurer.class, RETURNS_SELF);
        rememberCaptor.getValue().customize(rememberConfig);
        verify(rememberConfig).key("uniqueAndSecret");
    }
}