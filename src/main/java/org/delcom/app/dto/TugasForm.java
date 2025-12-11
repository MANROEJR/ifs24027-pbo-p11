package org.delcom.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TugasForm {

    private UUID id;

    @NotBlank(message = "Judul tugas harus diisi")
    private String judul;

    @NotBlank(message = "Mata kuliah harus diisi")
    private String mataKuliah;

    private String deskripsi;

    @NotNull(message = "Deadline harus diisi")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadline;

    private MultipartFile fileGambar;

    private String existingFoto;

    private String status = "Belum Selesai";

    public TugasForm() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getMataKuliah() {
        return mataKuliah;
    }

    public void setMataKuliah(String mataKuliah) {
        this.mataKuliah = mataKuliah;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public MultipartFile getFileGambar() {
        return fileGambar;
    }

    public void setFileGambar(MultipartFile fileGambar) {
        this.fileGambar = fileGambar;
    }

    public String getExistingFoto() {
        return existingFoto;
    }

    public void setExistingFoto(String existingFoto) {
        this.existingFoto = existingFoto;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Logic Methods

    public boolean hasImage() {
        return fileGambar != null && !fileGambar.isEmpty();
    }

    public boolean isValidImageFormat() {
        if (!hasImage()) return true; 
        String type = fileGambar.getContentType();
        return type != null && (type.equals("image/jpeg") || type.equals("image/png") || type.equals("image/webp"));
    }
}