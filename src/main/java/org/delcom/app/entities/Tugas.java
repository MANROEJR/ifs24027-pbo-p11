package org.delcom.app.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.*;

@Entity
@Table(name = "tugas")
@JsonPropertyOrder({ "id", "judul", "mataKuliah", "deadline", "status", "createdAt", "updatedAt" })
public class Tugas {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "judul", nullable = false)
    private String judul;

    @Column(name = "mata_kuliah", nullable = false)
    private String mataKuliah;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;

    @Column(name = "deadline")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") 
    private LocalDateTime deadline;

    @Column(name = "foto_bukti")
    private String fotoBukti;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Tugas() {
    }

    public Tugas(String judul, String mataKuliah, String deskripsi, LocalDateTime deadline, User user) {
        this.judul = judul;
        this.mataKuliah = mataKuliah;
        this.deskripsi = deskripsi;
        this.deadline = deadline;
        this.user = user;
        this.status = "Belum Selesai";
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

    public String getFotoBukti() {
        return fotoBukti;
    }

    public void setFotoBukti(String fotoBukti) {
        this.fotoBukti = fotoBukti;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // UBAH KE PUBLIC AGAR BISA DITEST UNIT TEST BIASA
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "Belum Selesai";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}