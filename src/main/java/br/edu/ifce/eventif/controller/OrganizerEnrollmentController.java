package br.edu.ifce.eventif.controller;

import br.edu.ifce.eventif.service.EnrollmentService;
import java.security.Principal;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/organizador/eventos/{eventId}/atividades/{activityId}/inscricoes")
public class OrganizerEnrollmentController {
    private final EnrollmentService enrollments;

    public OrganizerEnrollmentController(EnrollmentService enrollments) {
        this.enrollments = enrollments;
    }

    @GetMapping
    public String list(@PathVariable Long eventId, @PathVariable Long activityId, Principal principal, Model model) {
        model.addAttribute("roster", enrollments.roster(eventId, activityId, principal.getName()));
        return "organizador/atividades/inscritos";
    }

    @PutMapping("/presenca")
    public String presence(@PathVariable Long eventId, @PathVariable Long activityId,
            @RequestParam(name = "presentes", required = false) Set<Long> presentIds, Principal principal,
            RedirectAttributes redirect) {
        try {
            enrollments.updatePresence(eventId, activityId, presentIds, principal.getName());
            redirect.addFlashAttribute("success", "Presenças atualizadas.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/organizador/eventos/" + eventId + "/atividades/" + activityId + "/inscricoes";
    }
}
