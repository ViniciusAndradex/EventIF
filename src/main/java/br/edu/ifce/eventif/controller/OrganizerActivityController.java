package br.edu.ifce.eventif.controller;

import br.edu.ifce.eventif.dto.ActivityForm;
import br.edu.ifce.eventif.entity.ActivityType;
import br.edu.ifce.eventif.service.ActivityService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/organizador/eventos/{eventId}/atividades")
public class OrganizerActivityController {
    private final ActivityService activities;

    public OrganizerActivityController(ActivityService activities) {
        this.activities = activities;
    }

    @GetMapping
    public String list(@PathVariable Long eventId, Principal user, Model model) {
        model.addAttribute("event", activities.event(eventId, user.getName()));
        model.addAttribute("activities", activities.list(eventId, user.getName()));
        return "organizador/atividades/lista";
    }

    @GetMapping("/nova")
    public String createForm(@PathVariable Long eventId, Principal user, Model model, RedirectAttributes redirect) {
        try {
            activities.editableEvent(eventId, user.getName());
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return destination(eventId);
        }
        model.addAttribute("activityForm", new ActivityForm());
        return form(eventId, model);
    }

    @GetMapping("/{activityId}/editar")
    public String editForm(@PathVariable Long eventId, @PathVariable Long activityId, Principal user, Model model,
            RedirectAttributes redirect) {
        try {
            model.addAttribute("activityForm", activities.editForm(eventId, activityId, user.getName()));
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return destination(eventId);
        }
        model.addAttribute("activityId", activityId);
        return form(eventId, model);
    }

    @PostMapping
    public String create(@PathVariable Long eventId, @Valid @ModelAttribute("activityForm") ActivityForm form,
            BindingResult errors, Principal user, Model model, RedirectAttributes redirect) {
        activities.event(eventId, user.getName());
        if (!errors.hasErrors()) {
            try {
                activities.create(eventId, form, user.getName());
            } catch (IllegalArgumentException ex) {
                errors.reject("activity.invalid", ex.getMessage());
            }
        }
        if (errors.hasErrors())
            return form(eventId, model);
        redirect.addFlashAttribute("success", "Atividade criada.");
        return destination(eventId);
    }

    @PutMapping("/{activityId}")
    public String update(@PathVariable Long eventId, @PathVariable Long activityId,
            @Valid @ModelAttribute("activityForm") ActivityForm form, BindingResult errors, Principal user, Model model,
            RedirectAttributes redirect) {
        activities.find(eventId, activityId, user.getName());
        if (!errors.hasErrors()) {
            try {
                activities.update(eventId, activityId, form, user.getName());
            } catch (IllegalArgumentException ex) {
                errors.reject("activity.invalid", ex.getMessage());
            }
        }
        if (errors.hasErrors()) {
            model.addAttribute("activityId", activityId);
            return form(eventId, model);
        }
        redirect.addFlashAttribute("success", "Atividade atualizada.");
        return destination(eventId);
    }

    @DeleteMapping("/{activityId}")
    public String delete(@PathVariable Long eventId, @PathVariable Long activityId, Principal user,
            RedirectAttributes redirect) {
        try {
            activities.delete(eventId, activityId, user.getName());
            redirect.addFlashAttribute("success", "Atividade excluída.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return destination(eventId);
    }

    private String form(Long eventId, Model model) {
        model.addAttribute("eventId", eventId);
        model.addAttribute("types", ActivityType.values());
        return "organizador/atividades/formulario";
    }

    private String destination(Long eventId) {
        return "redirect:/organizador/eventos/" + eventId + "/atividades";
    }
}
