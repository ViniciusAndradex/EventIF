package br.edu.ifce.eventif.controller;

import br.edu.ifce.eventif.dto.CategoryForm;
import br.edu.ifce.eventif.exception.CategoryInUseException;
import br.edu.ifce.eventif.service.CategoryService;
import jakarta.validation.Valid;
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
@RequestMapping("/admin/categorias")
public class CategoryController {
    private final CategoryService categories;

    public CategoryController(CategoryService categories) { this.categories = categories; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categories.list());
        return "admin/categorias/lista";
    }

    @GetMapping("/nova")
    public String newForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        return "admin/categorias/formulario";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) return "admin/categorias/formulario";
        categories.create(form);
        redirect.addFlashAttribute("success", "Categoria criada com sucesso.");
        return "redirect:/admin/categorias";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        CategoryForm form = new CategoryForm();
        form.setName(categories.find(id).getName());
        model.addAttribute("categoryForm", form);
        model.addAttribute("categoryId", id);
        return "admin/categorias/formulario";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        categories.find(id);
        if (errors.hasErrors()) {
            model.addAttribute("categoryId", id);
            return "admin/categorias/formulario";
        }
        categories.update(id, form);
        redirect.addFlashAttribute("success", "Categoria atualizada com sucesso.");
        return "redirect:/admin/categorias";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            categories.delete(id);
            redirect.addFlashAttribute("success", "Categoria excluída com sucesso.");
        } catch (CategoryInUseException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/categorias";
    }
}
