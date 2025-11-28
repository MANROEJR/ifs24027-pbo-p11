package org.delcom.app.views;

import java.util.List;
import java.util.UUID;

import org.delcom.app.entities.CashFlow;
import org.delcom.app.entities.User;
import org.delcom.app.services.CashFlowService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cash-flows")
public class CashFlowView {

    private final CashFlowService cashFlowService;

    public CashFlowView(CashFlowService cashFlowService) {
        this.cashFlowService = cashFlowService;
    }

    // Helper method untuk cek user login (biar kodenya tidak berulang-ulang)
    private User getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return null;
        }
        return (User) principal;
    }

    // ==========================================
    // 1. LIST PAGE (HOME)
    // ==========================================
    @GetMapping
    public String listCashFlows(@RequestParam(required = false) String search, Model model) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/logout";

        // Ambil Data
        List<CashFlow> cashFlows = cashFlowService.getAllCashFlows(user.getId(), search);

        // Hitung Summary
        int totalIn = cashFlows.stream().filter(c -> "CASH_IN".equals(c.getType())).mapToInt(CashFlow::getAmount).sum();
        int totalOut = cashFlows.stream().filter(c -> "CASH_OUT".equals(c.getType())).mapToInt(CashFlow::getAmount).sum();

        model.addAttribute("cashFlows", cashFlows);
        model.addAttribute("search", search);
        model.addAttribute("totalIn", totalIn);
        model.addAttribute("totalOut", totalOut);
        model.addAttribute("balance", totalIn - totalOut);
        model.addAttribute("user", user);

        return "pages/cash-flows/home";
    }

    // ==========================================
    // 2. ADD PAGE (FORM)
    // ==========================================
    @GetMapping("/add")
    public String addCashFlowPage(Model model) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/logout";

        model.addAttribute("cashFlow", new CashFlow());
        return "pages/cash-flows/form";
    }

    // ==========================================
    // 3. PROCESS ADD
    // ==========================================
    @PostMapping("/add")
    public String postAddCashFlow(@ModelAttribute CashFlow cashFlow, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/logout";

        // Validasi Sederhana
        if (cashFlow.getAmount() == null || cashFlow.getAmount() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Nominal harus lebih dari 0");
            return "redirect:/cash-flows/add";
        }

        cashFlowService.createCashFlow(
                user.getId(),
                cashFlow.getType(),
                cashFlow.getSource(),
                cashFlow.getLabel(),
                cashFlow.getAmount(),
                cashFlow.getDescription()
        );

        redirectAttributes.addFlashAttribute("success", "Transaksi berhasil ditambahkan.");
        return "redirect:/cash-flows";
    }

    // ==========================================
    // 4. EDIT PAGE (FORM)
    // ==========================================
    @GetMapping("/edit/{id}")
    public String editCashFlowPage(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/logout";

        CashFlow cashFlow = cashFlowService.getCashFlowById(user.getId(), id);
        if (cashFlow == null) {
            redirectAttributes.addFlashAttribute("error", "Data tidak ditemukan.");
            return "redirect:/cash-flows";
        }

        model.addAttribute("cashFlow", cashFlow);
        return "pages/cash-flows/form";
    }

    // ==========================================
    // 5. PROCESS EDIT
    // ==========================================
    @PostMapping("/edit/{id}")
    public String postEditCashFlow(@PathVariable UUID id, @ModelAttribute CashFlow cashFlow, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/logout";

        cashFlowService.updateCashFlow(
                user.getId(), id,
                cashFlow.getType(),
                cashFlow.getSource(),
                cashFlow.getLabel(),
                cashFlow.getAmount(),
                cashFlow.getDescription()
        );

        redirectAttributes.addFlashAttribute("success", "Transaksi berhasil diperbarui.");
        return "redirect:/cash-flows";
    }

    // ==========================================
    // 6. PROCESS DELETE
    // ==========================================
    @PostMapping("/delete/{id}")
    public String postDeleteCashFlow(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/logout";

        cashFlowService.deleteCashFlow(user.getId(), id);
        
        redirectAttributes.addFlashAttribute("success", "Transaksi berhasil dihapus.");
        return "redirect:/cash-flows";
    }
}