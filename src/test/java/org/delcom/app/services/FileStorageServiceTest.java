package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.multipart.MultipartFile;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private final Path uploadPath = Paths.get("uploads");

    @BeforeEach
    void setUp() throws IOException {
        deleteDir(uploadPath); // Reset kondisi folder "uploads" (hapus jika ada)
    }

    @AfterEach
    void tearDown() throws IOException {
        deleteDir(uploadPath); // Bersihkan setelah test
    }

    private void deleteDir(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try { Files.delete(p); } catch (IOException e) {}
                });
        }
    }

    // --- TEST CONSTRUCTOR BRANCHES (INI KUNCINYA) ---

    @Test
    void testConstructor_DirectoryNotExists() throws IOException {
        // Kondisi: Folder 'uploads' belum ada (karena sudah dihapus di setUp)
        new FileStorageService();
        
        // Assert: Folder berhasil dibuat
        assertTrue(Files.exists(uploadPath));
    }

    @Test
    void testConstructor_DirectoryAlreadyExists() throws IOException {
        // Kondisi: Buat folder SECARA MANUAL dulu
        Files.createDirectories(uploadPath);
        
        // Act: Panggil constructor. Logika if (!exists) harusnya bernilai FALSE dan skip creation.
        new FileStorageService();
        
        // Assert: Tidak error dan folder tetap ada
        assertTrue(Files.exists(uploadPath));
    }

    // --- TEST STORE FILE ---

    @Test
    void testStoreFile_Success() throws IOException {
        fileStorageService = new FileStorageService();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String filename = fileStorageService.storeFile(mockFile, UUID.randomUUID());
        assertNotNull(filename);
    }

    @Test
    void testStoreFile_Empty() throws IOException {
        fileStorageService = new FileStorageService();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        assertThrows(IOException.class, () -> 
            fileStorageService.storeFile(mockFile, UUID.randomUUID())
        );
    }

    // --- TEST DELETE FILE BRANCHES ---

    @Test
    void testDeleteFile_Null() throws IOException {
        fileStorageService = new FileStorageService();
        assertDoesNotThrow(() -> fileStorageService.deleteFile(null));
    }

    @Test
    void testDeleteFile_Blank() throws IOException {
        fileStorageService = new FileStorageService();
        assertDoesNotThrow(() -> fileStorageService.deleteFile(""));
    }

    @Test
    void testDeleteFile_Valid() throws IOException {
        fileStorageService = new FileStorageService();
        assertDoesNotThrow(() -> fileStorageService.deleteFile("file_tidak_ada.txt"));
    }

    // --- TEST DELETE FILE EXCEPTION ---

    @Test
    void testDeleteFile_IOException() throws IOException {
        fileStorageService = new FileStorageService();
        
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Mock exists (dipanggil constructor)
            filesMock.when(() -> Files.exists(any())).thenReturn(true);
            
            // Mock deleteIfExists agar melempar Exception
            filesMock.when(() -> Files.deleteIfExists(any())).thenThrow(new IOException("Disk Error"));

            // Assert RuntimeException dilempar
            assertThrows(RuntimeException.class, () -> 
                fileStorageService.deleteFile("file.txt")
            );
        }
    }
}