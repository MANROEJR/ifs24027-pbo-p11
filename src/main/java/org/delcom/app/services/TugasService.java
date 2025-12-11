package org.delcom.app.services;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.delcom.app.entities.Tugas;
import org.delcom.app.repositories.TugasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TugasService {

    private final TugasRepository tugasRepository;
    private final FileStorageService fileStorageService;

    public TugasService(TugasRepository tugasRepository, FileStorageService fileStorageService) {
        this.tugasRepository = tugasRepository;
        this.fileStorageService = fileStorageService;
    }

    // Mengambil daftar tugas user, urut deadline terdekat
    @Transactional(readOnly = true)
    public List<Tugas> getAllTugas(UUID userId) {
        return tugasRepository.findAllByUserIdOrderByDeadlineAsc(userId);
    }

    // Mengambil satu tugas detail
    @Transactional(readOnly = true)
    public Tugas getTugasById(UUID id) {
        return tugasRepository.findById(id).orElse(null);
    }

    // Menambah Tugas Baru + Upload Gambar
    @Transactional
    public Tugas createTugas(Tugas tugas, MultipartFile file) throws IOException {
        // 1. Simpan dulu untuk dapat ID (UUID)
        Tugas savedTugas = tugasRepository.save(tugas);

        // 2. Jika ada file gambar, upload & update nama file di database
        if (file != null && !file.isEmpty()) {
            String filename = fileStorageService.storeFile(file, savedTugas.getId());
            savedTugas.setFotoBukti(filename);
            return tugasRepository.save(savedTugas); // Update lagi
        }

        return savedTugas;
    }

    // Update Tugas + Ganti Gambar (Opsional)
    @Transactional
    public Tugas updateTugas(UUID id, Tugas tugasDetails, MultipartFile file) throws IOException {
        Tugas existingTugas = getTugasById(id);
        if (existingTugas == null) return null;

        // Update Data Text
        existingTugas.setJudul(tugasDetails.getJudul());
        existingTugas.setMataKuliah(tugasDetails.getMataKuliah());
        existingTugas.setDeskripsi(tugasDetails.getDeskripsi());
        existingTugas.setDeadline(tugasDetails.getDeadline());

        // Update Gambar jika user mengupload file baru
        if (file != null && !file.isEmpty()) {
            // Hapus file lama jika ada
            fileStorageService.deleteFile(existingTugas.getFotoBukti());
            
            // Upload file baru
            String newFilename = fileStorageService.storeFile(file, existingTugas.getId());
            existingTugas.setFotoBukti(newFilename);
        }

        return tugasRepository.save(existingTugas);
    }

    // Update Status Saja (Selesai/Belum)
    @Transactional
    public Tugas updateStatus(UUID id, String status) {
        Tugas tugas = getTugasById(id);
        if (tugas != null) {
            tugas.setStatus(status);
            return tugasRepository.save(tugas);
        }
        return null;
    }

    // Hapus Tugas + Hapus File Gambar
    @Transactional
    public void deleteTugas(UUID id) {
        Tugas tugas = getTugasById(id);
        if (tugas != null) {
            // Hapus file fisiknya
            fileStorageService.deleteFile(tugas.getFotoBukti());
            // Hapus data di DB
            tugasRepository.delete(tugas);
        }
    }
}