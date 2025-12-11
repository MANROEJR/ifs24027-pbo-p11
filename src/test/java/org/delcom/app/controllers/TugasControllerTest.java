package org.delcom.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Tugas;
import org.delcom.app.entities.User;
import org.delcom.app.services.TugasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class TugasControllerTests {

    @Mock
    private TugasService tugasService;

    @Mock
    private AuthContext authContext;

    @Mock
    private MultipartFile mockFile;

    private TugasController tugasController;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // INISIALISASI MANUAL AGAR PASTI TER-INJECT
        tugasController = new TugasController(tugasService, authContext);
        
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("Test User");
    }

    // --- 1. GET ALL ---
    @Test
    void testGetAllTugas_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(tugasService.getAllTugas(mockUser.getId())).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse<List<Tugas>>> response = tugasController.getAllTugas();
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetAllTugas_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<List<Tugas>>> response = tugasController.getAllTugas();
        assertEquals(401, response.getStatusCode().value());
    }

    // --- 2. CREATE ---
    @Test
    void testCreateTugas_Success() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        
        Tugas t = new Tugas();
        t.setJudul("Judul");
        when(tugasService.createTugas(any(Tugas.class), any())).thenReturn(t);

        ResponseEntity<ApiResponse<Tugas>> response = tugasController.createTugas(
            "Judul", "Matkul", "Desk", "2025-12-31T10:00", mockFile
        );
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateTugas_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Tugas>> response = tugasController.createTugas(
            "J", "M", "D", "2025-12-31T10:00", null
        );
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateTugas_BadDate() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);

        // Memicu catch(Exception) karena format tanggal salah
        ResponseEntity<ApiResponse<Tugas>> response = tugasController.createTugas(
            "J", "M", "D", "BukanTanggal", null
        );
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testCreateTugas_IOException() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);

        // Memicu catch(IOException)
        when(tugasService.createTugas(any(Tugas.class), any())).thenThrow(new IOException("Full"));

        ResponseEntity<ApiResponse<Tugas>> response = tugasController.createTugas(
            "J", "M", "D", "2025-12-31T10:00", mockFile
        );
        assertEquals(500, response.getStatusCode().value());
    }

    // --- 3. UPDATE ---
    @Test
    void testUpdateTugas_Success() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        UUID id = UUID.randomUUID();
        Tugas t = new Tugas();
        
        when(tugasService.updateTugas(eq(id), any(Tugas.class), any())).thenReturn(t);

        ResponseEntity<ApiResponse<Tugas>> response = tugasController.updateTugas(
            id, "J", "M", "D", "2025-12-31T10:00", mockFile
        );
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testUpdateTugas_NotFound() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        UUID id = UUID.randomUUID();

        // Return null -> Masuk if (updatedTugas == null)
        when(tugasService.updateTugas(eq(id), any(Tugas.class), any())).thenReturn(null);

        ResponseEntity<ApiResponse<Tugas>> response = tugasController.updateTugas(
            id, "J", "M", "D", "2025-12-31T10:00", mockFile
        );
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateTugas_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Tugas>> response = tugasController.updateTugas(
            UUID.randomUUID(), "J", "M", "D", "T", null
        );
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateTugas_BadDate() {
        when(authContext.isAuthenticated()).thenReturn(true);
        ResponseEntity<ApiResponse<Tugas>> response = tugasController.updateTugas(
            UUID.randomUUID(), "J", "M", "D", "Rusak", null
        );
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testUpdateTugas_IOException() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        UUID id = UUID.randomUUID();

        when(tugasService.updateTugas(eq(id), any(Tugas.class), any())).thenThrow(new IOException("Err"));

        ResponseEntity<ApiResponse<Tugas>> response = tugasController.updateTugas(
            id, "J", "M", "D", "2025-12-31T10:00", mockFile
        );
        assertEquals(500, response.getStatusCode().value());
    }

    // --- 4. PATCH STATUS ---
    @Test
    void testUpdateStatus_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        UUID id = UUID.randomUUID();
        Tugas t = new Tugas();
        
        when(tugasService.updateStatus(id, "Done")).thenReturn(t);

        ResponseEntity<ApiResponse<Tugas>> response = tugasController.updateStatus(id, "Done");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testUpdateStatus_NotFound() {
        when(authContext.isAuthenticated()).thenReturn(true);
        UUID id = UUID.randomUUID();

        // Return null -> Masuk if (updatedTugas == null)
        when(tugasService.updateStatus(id, "Done")).thenReturn(null);

        ResponseEntity<ApiResponse<Tugas>> response = tugasController.updateStatus(id, "Done");
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateStatus_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Tugas>> response = tugasController.updateStatus(UUID.randomUUID(), "S");
        assertEquals(401, response.getStatusCode().value());
    }

    // --- 5. DELETE ---
    @Test
    void testDeleteTugas_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        UUID id = UUID.randomUUID();
        ResponseEntity<ApiResponse<Void>> response = tugasController.deleteTugas(id);
        assertEquals(200, response.getStatusCode().value());
        verify(tugasService, times(1)).deleteTugas(id);
    }

    @Test
    void testDeleteTugas_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Void>> response = tugasController.deleteTugas(UUID.randomUUID());
        assertEquals(401, response.getStatusCode().value());
        verify(tugasService, times(0)).deleteTugas(any());
    }
}