package org.delcom.app.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Tugas;
import org.delcom.app.entities.User;
import org.delcom.app.services.TugasService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tugas")
public class TugasController {

    private final TugasService tugasService;
    private final AuthContext authContext; 

    // CONSTRUCTOR INJECTION (Kunci agar Test bisa jalan 100%)
    public TugasController(TugasService tugasService, AuthContext authContext) {
        this.tugasService = tugasService;
        this.authContext = authContext;
    }

    // 1. GET ALL
    @GetMapping
    public ResponseEntity<ApiResponse<List<Tugas>>> getAllTugas() {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }
        
        User user = authContext.getAuthUser();
        List<Tugas> tugasList = tugasService.getAllTugas(user.getId());
        
        return ResponseEntity.ok(new ApiResponse<>("success", "Berhasil mengambil data tugas", tugasList));
    }

    // 2. CREATE
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Tugas>> createTugas(
            @RequestParam("judul") String judul,
            @RequestParam("mataKuliah") String mataKuliah,
            @RequestParam(value = "deskripsi", required = false) String deskripsi,
            @RequestParam("deadline") String deadlineStr, 
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }

        try {
            User user = authContext.getAuthUser();
            // Parse bisa error -> Masuk catch(Exception)
            LocalDateTime deadline = LocalDateTime.parse(deadlineStr);

            Tugas newTugas = new Tugas(judul, mataKuliah, deskripsi, deadline, user);
            
            // Service bisa error IO -> Masuk catch(IOException)
            Tugas savedTugas = tugasService.createTugas(newTugas, file);

            return ResponseEntity.ok(new ApiResponse<>("success", "Tugas berhasil ditambahkan", savedTugas));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Gagal upload gambar", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Format data salah (pastikan tanggal formatnya 'YYYY-MM-DDTHH:MM'): " + e.getMessage(), null));
        }
    }

    // 3. UPDATE
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Tugas>> updateTugas(
            @PathVariable UUID id,
            @RequestParam("judul") String judul,
            @RequestParam("mataKuliah") String mataKuliah,
            @RequestParam(value = "deskripsi", required = false) String deskripsi,
            @RequestParam("deadline") String deadlineStr,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }

        try {
            LocalDateTime deadline = LocalDateTime.parse(deadlineStr);
            
            Tugas tugasDetails = new Tugas();
            tugasDetails.setJudul(judul);
            tugasDetails.setMataKuliah(mataKuliah);
            tugasDetails.setDeskripsi(deskripsi);
            tugasDetails.setDeadline(deadline);

            Tugas updatedTugas = tugasService.updateTugas(id, tugasDetails, file);

            if (updatedTugas == null) {
                return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Tugas tidak ditemukan", null));
            }

            return ResponseEntity.ok(new ApiResponse<>("success", "Tugas berhasil diupdate", updatedTugas));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Gagal proses gambar", null));
        } catch (Exception e) {
             return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Format tanggal salah", null));
        }
    }

    // 4. UPDATE STATUS
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Tugas>> updateStatus(
            @PathVariable UUID id, 
            @RequestBody String status 
    ) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }
        
        String cleanStatus = status.replace("\"", "").trim();

        Tugas updatedTugas = tugasService.updateStatus(id, cleanStatus);
        
        if (updatedTugas == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Tugas tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>("success", "Status berhasil diupdate", updatedTugas));
    }

    // 5. DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTugas(@PathVariable UUID id) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }

        tugasService.deleteTugas(id);
        
        return ResponseEntity.ok(new ApiResponse<>("success", "Tugas berhasil dihapus", null));
    }
}