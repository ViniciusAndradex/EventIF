package br.edu.ifce.eventif.service;

import br.edu.ifce.eventif.dto.CategoryForm;
import br.edu.ifce.eventif.entity.Category;
import br.edu.ifce.eventif.exception.CategoryInUseException;
import br.edu.ifce.eventif.repository.CategoryRepository;
import br.edu.ifce.eventif.repository.EventRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoryService {
    private final CategoryRepository categories;
    private final EventRepository events;

    public CategoryService(CategoryRepository categories, EventRepository events) {
        this.categories = categories;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<Category> list() {
        return categories.findAllByOrderByNameAscIdAsc();
    }

    @Transactional(readOnly = true)
    public Category find(Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada."));
    }

    @Transactional
    public void create(CategoryForm form) {
        categories.save(new Category(form.getName().strip()));
    }

    @Transactional
    public void update(Long id, CategoryForm form) {
        find(id).setName(form.getName().strip());
    }

    @Transactional
    public void delete(Long id) {
        Category category = find(id);
        if (events.existsByCategoryId(id)) {
            throw new CategoryInUseException();
        }
        try {
            categories.delete(category);
            categories.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new CategoryInUseException();
        }
    }
}
