package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Tugas;
import org.delcom.app.repositories.TugasRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class TugasServiceTests {

    @Mock private TugasRepository tugasRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private MultipartFile mockFile;
    
    @InjectMocks private TugasService tugasService;

    // --- 1. GET ALL ---
    @Test
    void testGetAllTugas() {
        UUID userId = UUID.randomUUID();
        when(tugasRepository.findAllByUserIdOrderByDeadlineAsc(userId)).thenReturn(Collections.emptyList());
        
        List<Tugas> result = tugasService.getAllTugas(userId);
        assertNotNull(result);
    }

    // --- 2. GET BY ID ---
    @Test
    void testGetTugasById_Found() {
        UUID id = UUID.randomUUID();
        when(tugasRepository.findById(id)).thenReturn(Optional.of(new Tugas()));
        
        Tugas result = tugasService.getTugasById(id);
        assertNotNull(result);
    }

    @Test
    void testGetTugasById_NotFound() {
        UUID id = UUID.randomUUID();
        when(tugasRepository.findById(id)).thenReturn(Optional.empty());
        
        Tugas result = tugasService.getTugasById(id);
        assertNull(result);
    }

    // --- 3. CREATE ---
    @Test
    @DisplayName("Create: Dengan File")
    void testCreateTugas_WithFile() throws IOException {
        Tugas t = new Tugas();
        t.setId(UUID.randomUUID());

        when(tugasRepository.save(any(Tugas.class))).thenReturn(t);
        when(mockFile.isEmpty()).thenReturn(false); // File Ada
        when(fileStorageService.storeFile(eq(mockFile), any())).thenReturn("img.jpg");

        tugasService.createTugas(t, mockFile);

        // Verifikasi storeFile dipanggil
        verify(fileStorageService).storeFile(eq(mockFile), any());
        // Verifikasi save dipanggil 2x (awal dan setelah update foto)
        verify(tugasRepository, times(2)).save(any(Tugas.class));
    }

    @Test
    @DisplayName("Create: Tanpa File (Null)")
    void testCreateTugas_NullFile() throws IOException {
        Tugas t = new Tugas();
        when(tugasRepository.save(any(Tugas.class))).thenReturn(t);

        tugasService.createTugas(t, null); // File Null

        verify(fileStorageService, never()).storeFile(any(), any());
        verify(tugasRepository, times(1)).save(any(Tugas.class));
    }

    @Test
    @DisplayName("Create: File Kosong (Object Ada)")
    void testCreateTugas_EmptyFile() throws IOException {
        Tugas t = new Tugas();
        when(tugasRepository.save(any(Tugas.class))).thenReturn(t);
        when(mockFile.isEmpty()).thenReturn(true); // File Empty

        tugasService.createTugas(t, mockFile);

        verify(fileStorageService, never()).storeFile(any(), any());
        verify(tugasRepository, times(1)).save(any(Tugas.class));
    }

    // --- 4. UPDATE ---
    @Test
    @DisplayName("Update: Not Found")
    void testUpdateTugas_NotFound() throws IOException {
        UUID id = UUID.randomUUID();
        when(tugasRepository.findById(id)).thenReturn(Optional.empty());

        Tugas res = tugasService.updateTugas(id, new Tugas(), null);
        assertNull(res);
    }

    @Test
    @DisplayName("Update: Found & File Baru")
    void testUpdateTugas_WithFile() throws IOException {
        UUID id = UUID.randomUUID();
        Tugas existing = new Tugas();
        existing.setId(id);
        existing.setFotoBukti("old.jpg");

        when(tugasRepository.findById(id)).thenReturn(Optional.of(existing));
        when(mockFile.isEmpty()).thenReturn(false); // File Ada
        when(fileStorageService.storeFile(eq(mockFile), any())).thenReturn("new.jpg");
        when(tugasRepository.save(existing)).thenReturn(existing);

        Tugas result = tugasService.updateTugas(id, new Tugas(), mockFile);

        assertEquals("new.jpg", result.getFotoBukti());
        verify(fileStorageService).deleteFile("old.jpg"); // Pastikan file lama dihapus
    }

    @Test
    @DisplayName("Update: Found & File Null")
    void testUpdateTugas_NullFile() throws IOException {
        UUID id = UUID.randomUUID();
        Tugas existing = new Tugas();
        
        when(tugasRepository.findById(id)).thenReturn(Optional.of(existing));
        when(tugasRepository.save(existing)).thenReturn(existing);

        tugasService.updateTugas(id, new Tugas(), null);

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    @DisplayName("Update: Found & File Empty")
    void testUpdateTugas_EmptyFile() throws IOException {
        UUID id = UUID.randomUUID();
        Tugas existing = new Tugas();
        
        when(tugasRepository.findById(id)).thenReturn(Optional.of(existing));
        when(tugasRepository.save(existing)).thenReturn(existing);
        when(mockFile.isEmpty()).thenReturn(true); // File Empty

        tugasService.updateTugas(id, new Tugas(), mockFile);

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    // --- 5. UPDATE STATUS ---
    @Test
    void testUpdateStatus_Found() {
        UUID id = UUID.randomUUID();
        Tugas t = new Tugas();
        when(tugasRepository.findById(id)).thenReturn(Optional.of(t));
        when(tugasRepository.save(t)).thenReturn(t);

        Tugas res = tugasService.updateStatus(id, "Selesai");
        assertEquals("Selesai", res.getStatus());
    }

    @Test
    void testUpdateStatus_NotFound() {
        UUID id = UUID.randomUUID();
        when(tugasRepository.findById(id)).thenReturn(Optional.empty());

        Tugas res = tugasService.updateStatus(id, "Selesai");
        assertNull(res);
    }

    // --- 6. DELETE ---
    @Test
    void testDeleteTugas_Found() {
        UUID id = UUID.randomUUID();
        Tugas t = new Tugas();
        t.setFotoBukti("pic.jpg");

        when(tugasRepository.findById(id)).thenReturn(Optional.of(t));

        tugasService.deleteTugas(id);

        verify(fileStorageService).deleteFile("pic.jpg");
        verify(tugasRepository).delete(t);
    }

    @Test
    void testDeleteTugas_NotFound() {
        UUID id = UUID.randomUUID();
        when(tugasRepository.findById(id)).thenReturn(Optional.empty());

        tugasService.deleteTugas(id);

        verify(tugasRepository, never()).delete(any());
        verify(fileStorageService, never()).deleteFile(any());
    }
}