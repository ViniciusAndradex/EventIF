package br.edu.ifce.eventif.controller;

import br.edu.ifce.eventif.service.EnrollmentService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/organizador/eventos/{eventId}/atividades/{activityId}/inscricoes")
public class OrganizerEnrollmentController {
    private final EnrollmentService enrollments;

    public OrganizerEnrollmentController(EnrollmentService enrollments) { this.enrollments = enrollments; }

    @GetMapping
    public String list(@PathVariable Long eventId, @PathVariable Long activityId, Principal principal, Model model) {
        model.addAttribute("roster", enrollments.roster(eventId, activityId, principal.getName()));
        return "organizador/atividades/inscritos";
    }

    @PutMapping("/{id}/presenca")
    public String present(@PathVariable Long eventId, @PathVariable Long activityId, @PathVariable Long id,
                          Principal principal, RedirectAttributes redirect) {
        try {
            enrollments.markPresent(eventId, activityId, id, principal.getName());
            redirect.addFlashAttribute("success", "Presença registrada.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/organizador/eventos/" + eventId + "/atividades/" + activityId + "/inscricoes";
    }
}
