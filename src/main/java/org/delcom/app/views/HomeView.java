package org.delcom.app.views;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeView {

    @GetMapping
    public String home() {
        // Redirect langsung ke halaman daftar tugas
        return "redirect:/tugas";
    }
}