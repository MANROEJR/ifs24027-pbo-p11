package org.delcom.app.views;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.delcom.app.entities.Tugas;
import org.delcom.app.entities.User;
import org.delcom.app.services.TugasService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tugas")
public class TugasView {

    private final TugasService tugasService;

    public TugasView(TugasService tugasService) {
        this.tugasService = tugasService;
    }

    // --- HELPER METHOD: Ambil User dari Session Login ---
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
        }
        return null;
    }

    // 1. Tampilkan Halaman Daftar Tugas + Statistik
    @GetMapping
    public String showList(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/auth/login";
        
        // Ambil data tugas dari database
        List<Tugas> listTugas = tugasService.getAllTugas(user.getId());
        
        // --- LOGIKA HITUNG STATISTIK (DASHBOARD) ---
        int totalTugas = listTugas.size();
        int statSelesai = 0;
        int statProses = 0;
        int statTelat = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Tugas t : listTugas) {
            if ("Selesai".equalsIgnoreCase(t.getStatus())) {
                statSelesai++;
            } else {
                // Jika belum selesai, cek apakah sudah lewat deadline?
                if (t.getDeadline() != null && t.getDeadline().isBefore(now)) {
                    statTelat++; // Telat
                } else {
                    statProses++; // Masih Proses (Waktu masih ada)
                }
            }
        }

        // Kirim Data List Tugas ke HTML
        model.addAttribute("listTugas", listTugas);
        model.addAttribute("userName", user.getName()); // Untuk Halo, User
        
        // Kirim Data Statistik ke HTML (untuk Kartu & Grafik)
        model.addAttribute("totalTugas", totalTugas);
        model.addAttribute("statSelesai", statSelesai);
        model.addAttribute("statProses", statProses);
        model.addAttribute("statTelat", statTelat);
        
        return "pages/tugas/index"; 
    }

    // 2. Tampilkan Form Tambah Tugas
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User user = getAuthenticatedUser(); // Tangkap user dulu
        if (user == null) return "redirect:/auth/login";
        
        model.addAttribute("tugas", new Tugas());
        model.addAttribute("userName", user.getName()); // <--- TAMBAHAN: Agar Navbar tetap ada nama
        
        return "pages/tugas/create";
    }

    // 3. Proses Simpan Tugas Baru
    @PostMapping("/store")
    public String storeTugas(
            @ModelAttribute Tugas tugas, 
            @RequestParam("fileGambar") MultipartFile file, 
            RedirectAttributes redirectAttributes
    ) {
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/auth/login";

        try {
            tugas.setUser(user);
            tugasService.createTugas(tugas, file);
            redirectAttributes.addFlashAttribute("success", "Tugas berhasil ditambahkan!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengupload gambar.");
        }
        return "redirect:/tugas";
    }

    // 4. Tampilkan Form Edit
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model) {
        User user = getAuthenticatedUser(); // Tangkap user dulu
        if (user == null) return "redirect:/auth/login";

        Tugas tugas = tugasService.getTugasById(id);
        if (tugas == null) return "redirect:/tugas";

        model.addAttribute("tugas", tugas);
        model.addAttribute("userName", user.getName()); // <--- TAMBAHAN: Agar Navbar tetap ada nama
        
        return "pages/tugas/edit";
    }

    // 5. Proses Update Tugas
    @PostMapping("/update/{id}")
    public String updateTugas(
            @PathVariable UUID id, 
            @ModelAttribute Tugas tugas, 
            @RequestParam(value = "fileGambar", required = false) MultipartFile file, 
            RedirectAttributes redirectAttributes
    ) {
        if (getAuthenticatedUser() == null) return "redirect:/auth/login";

        try {
            tugasService.updateTugas(id, tugas, file);
            redirectAttributes.addFlashAttribute("success", "Tugas berhasil diperbarui!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal update gambar.");
        }
        return "redirect:/tugas";
    }

    // 6. Proses Delete
    @PostMapping("/delete/{id}")
    public String deleteTugas(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        if (getAuthenticatedUser() == null) return "redirect:/auth/login";

        tugasService.deleteTugas(id);
        redirectAttributes.addFlashAttribute("success", "Tugas berhasil dihapus.");
        return "redirect:/tugas";
    }

    // 7. Proses Tandai Selesai
    @PostMapping("/{id}/selesai")
    public String tandaiSelesai(@PathVariable UUID id) {
        if (getAuthenticatedUser() == null) return "redirect:/auth/login";
        
        tugasService.updateStatus(id, "Selesai");
        return "redirect:/tugas";
    }
}