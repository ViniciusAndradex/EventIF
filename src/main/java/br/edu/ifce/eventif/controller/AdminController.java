package br.edu.ifce.eventif.controller;

import br.edu.ifce.eventif.service.UserAdministrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserAdministrationService administration;

    public AdminController(UserAdministrationService administration) {
        this.administration = administration;
    }

    @GetMapping("")
    public String users(Model model) {
        model.addAttribute("users", administration.listUsers());
        return "admin/usuarios";
    }

    @PutMapping("/usuarios/{id}/organizador")
    public String grantOrganizer(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            administration.grantOrganizer(id);
            redirect.addFlashAttribute("success", "Papel de organizador concedido.");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin";
    }

    @DeleteMapping("/usuarios/{id}/organizador")
    public String revokeOrganizer(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            administration.revokeOrganizer(id);
            redirect.addFlashAttribute("success",
                    "Papel de organizador retirado; eventuais rascunhos foram encerrados.");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin";
    }
}
