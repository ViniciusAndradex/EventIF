package br.edu.ifce.eventif.controller;

import br.edu.ifce.eventif.service.EnrollmentService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inscricoes")
public class EnrollmentController {
    private final EnrollmentService enrollments;

    public EnrollmentController(EnrollmentService enrollments) { this.enrollments = enrollments; }

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("enrollments", enrollments.listMine(principal.getName()));
        return "inscricoes/lista";
    }

    @PostMapping("/atividades/{activityId}")
    public String create(@PathVariable Long activityId, Principal principal, RedirectAttributes redirect) {
        try {
            enrollments.enroll(activityId, principal.getName());
            redirect.addFlashAttribute("success", "Inscrição realizada.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/inscricoes";
    }

    @DeleteMapping("/{id}")
    public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            enrollments.cancel(id, principal.getName());
            redirect.addFlashAttribute("success", "Inscrição cancelada.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/inscricoes";
    }
}
