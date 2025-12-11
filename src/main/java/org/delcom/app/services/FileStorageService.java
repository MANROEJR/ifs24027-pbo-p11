package org.delcom.app.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() throws IOException {
        // Branch 1: Folder Belum Ada (True) -> Create
        // Branch 2: Folder Sudah Ada (False) -> Skip
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }
    }

    public String storeFile(MultipartFile file, UUID tugasId) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }
        
        String filename = tugasId.toString() + "_" + file.getOriginalFilename();
        Path destinationFile = this.rootLocation.resolve(filename).normalize().toAbsolutePath();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }
        
        return filename;
    }

    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Bungkus ke RuntimeException agar bisa dites dan dihitung coverage-nya
            throw new RuntimeException("Gagal menghapus file", e);
        }
    }
}