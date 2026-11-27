package br.edu.ifce.eventif.controller;

import br.edu.ifce.eventif.service.CatalogService;
import br.edu.ifce.eventif.service.CategoryService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    private final CatalogService catalog;
    private final CategoryService categories;

    public HomeController(CatalogService catalog, CategoryService categories) {
        this.catalog = catalog;
        this.categories = categories;
    }

    @GetMapping("/")
    public String home(@RequestParam(name = "categoria", required = false) Long categoryId,
            @RequestParam(name = "pagina", defaultValue = "0") int page, Model model) {
        model.addAttribute("events", catalog.list(categoryId, page));
        model.addAttribute("categories", categories.list());
        model.addAttribute("categoryId", categoryId);
        return "index";
    }

    @GetMapping("/eventos/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("detail", catalog.detail(id, principal == null ? null : principal.getName()));
        return "eventos/detalhe";
    }
}
