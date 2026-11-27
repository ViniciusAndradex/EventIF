package br.edu.ifce.eventif.controller;

import br.edu.ifce.eventif.dto.EventForm;
import br.edu.ifce.eventif.service.CategoryService;
import br.edu.ifce.eventif.service.EventService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/organizador")
public class OrganizerEventController {
    private final EventService events;
    private final CategoryService categories;

    public OrganizerEventController(EventService events, CategoryService categories) {
        this.events = events;
        this.categories = categories;
    }

    @GetMapping
    public String home() {
        return "redirect:/organizador/eventos";
    }

    @GetMapping("/eventos")
    public String list(Principal principal, Model model) {
        model.addAttribute("events", events.list(principal.getName()));
        return "organizador/eventos/lista";
    }

    @GetMapping("/eventos/novo")
    public String createForm(Model model) {
        model.addAttribute("eventForm", new EventForm());
        return form(model);
    }

    @GetMapping("/eventos/{id}/editar")
    public String editForm(@PathVariable Long id, Principal principal, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("eventForm", events.editForm(id, principal.getName()));
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/organizador/eventos";
        }
        model.addAttribute("eventId", id);
        return form(model);
    }

    @PostMapping("/eventos")
    public String create(@Valid @ModelAttribute("eventForm") EventForm eventForm, BindingResult errors,
            Principal principal, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                events.create(eventForm, principal.getName());
            } catch (IllegalArgumentException ex) {
                errors.reject("event.invalid", ex.getMessage());
            }
        }
        if (errors.hasErrors())
            return form(model);
        redirect.addFlashAttribute("success", "Evento criado como rascunho.");
        return "redirect:/organizador/eventos";
    }

    @PutMapping("/eventos/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("eventForm") EventForm eventForm,
            BindingResult errors, Principal principal, Model model, RedirectAttributes redirect) {
        events.findOwned(id, principal.getName());
        if (!errors.hasErrors()) {
            try {
                events.update(id, eventForm, principal.getName());
            } catch (IllegalArgumentException ex) {
                errors.reject("event.invalid", ex.getMessage());
            }
        }
        if (errors.hasErrors()) {
            model.addAttribute("eventId", id);
            return form(model);
        }
        redirect.addFlashAttribute("success", "Evento atualizado.");
        return "redirect:/organizador/eventos";
    }

    @PutMapping("/eventos/{id}/publicar")
    public String publish(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            events.publish(id, principal.getName());
            redirect.addFlashAttribute("success", "Evento publicado.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/organizador/eventos";
    }

    @PutMapping("/eventos/{id}/encerrar")
    public String close(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            events.close(id, principal.getName());
            redirect.addFlashAttribute("success", "Evento encerrado.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/organizador/eventos";
    }

    @DeleteMapping("/eventos/{id}")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            events.delete(id, principal.getName());
            redirect.addFlashAttribute("success", "Evento excluído.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/organizador/eventos";
    }

    private String form(Model model) {
        model.addAttribute("categories", categories.list());
        return "organizador/eventos/formulario";
    }
}
