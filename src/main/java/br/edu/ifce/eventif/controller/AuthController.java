package br.edu.ifce.eventif.controller;

import br.edu.ifce.eventif.dto.RegistrationForm;
import br.edu.ifce.eventif.service.UserService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String registration(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form, BindingResult errors,
            RedirectAttributes redirect) {
        if (errors.hasErrors())
            return "cadastro";
        try {
            users.register(form);
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            errors.rejectValue("email", "duplicate", "Este email já está cadastrado.");
            return "cadastro";
        }
        redirect.addFlashAttribute("message", "Cadastro realizado. Faça login.");
        return "redirect:/login";
    }
}
