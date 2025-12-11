package org.delcom.app.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class TugasFormTest {

    @Test
    void testGettersAndSetters() {
        TugasForm form = new TugasForm();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        MultipartFile mockFile = mock(MultipartFile.class);

        // Cek default status
        assertEquals("Belum Selesai", form.getStatus());

        // Set Values
        form.setId(id);
        form.setJudul("Judul Tugas");
        form.setMataKuliah("PBO");
        form.setDeskripsi("Deskripsi Tugas");
        form.setDeadline(now);
        form.setFileGambar(mockFile);
        form.setExistingFoto("foto.jpg");
        form.setStatus("Selesai");

        // Assert Getters
        assertEquals(id, form.getId());
        assertEquals("Judul Tugas", form.getJudul());
        assertEquals("PBO", form.getMataKuliah());
        assertEquals("Deskripsi Tugas", form.getDeskripsi());
        assertEquals(now, form.getDeadline());
        assertEquals(mockFile, form.getFileGambar());
        assertEquals("foto.jpg", form.getExistingFoto());
        assertEquals("Selesai", form.getStatus());
    }

    // --- TEST LOGIC: hasImage() ---

    @Test
    void testHasImage_False_WhenNull() {
        TugasForm form = new TugasForm();
        form.setFileGambar(null);
        
        assertFalse(form.hasImage(), "Harusnya false jika file null");
    }

    @Test
    void testHasImage_False_WhenEmpty() {
        TugasForm form = new TugasForm();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);
        form.setFileGambar(mockFile);
        
        assertFalse(form.hasImage(), "Harusnya false jika file empty");
    }

    @Test
    void testHasImage_True_WhenValid() {
        TugasForm form = new TugasForm();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        form.setFileGambar(mockFile);
        
        assertTrue(form.hasImage(), "Harusnya true jika file ada isinya");
    }

    // --- TEST LOGIC: isValidImageFormat() ---

    @Test
    void testIsValidImageFormat_True_WhenNoImage() {
        // Jika tidak ada gambar, dianggap valid (skip validasi)
        TugasForm form = new TugasForm();
        form.setFileGambar(null);

        assertTrue(form.isValidImageFormat());
    }

    @Test
    void testIsValidImageFormat_True_Jpeg() {
        TugasForm form = new TugasForm();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false); // hasImage = true
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        
        form.setFileGambar(mockFile);

        assertTrue(form.isValidImageFormat());
    }

    @Test
    void testIsValidImageFormat_True_Png() {
        TugasForm form = new TugasForm();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getContentType()).thenReturn("image/png");
        
        form.setFileGambar(mockFile);

        assertTrue(form.isValidImageFormat());
    }

    @Test
    void testIsValidImageFormat_True_Webp() {
        TugasForm form = new TugasForm();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getContentType()).thenReturn("image/webp");
        
        form.setFileGambar(mockFile);

        assertTrue(form.isValidImageFormat());
    }

    @Test
    void testIsValidImageFormat_False_InvalidType() {
        TugasForm form = new TugasForm();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getContentType()).thenReturn("application/pdf"); // Bukan gambar
        
        form.setFileGambar(mockFile);

        assertFalse(form.isValidImageFormat());
    }

    @Test
    void testIsValidImageFormat_False_NullContentType() {
        TugasForm form = new TugasForm();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getContentType()).thenReturn(null); // Content type tidak terdeteksi
        
        form.setFileGambar(mockFile);

        assertFalse(form.isValidImageFormat());
    }
}