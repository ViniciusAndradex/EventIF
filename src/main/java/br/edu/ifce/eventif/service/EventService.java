package br.edu.ifce.eventif.service;

import br.edu.ifce.eventif.dto.EventForm;
import br.edu.ifce.eventif.entity.Category;
import br.edu.ifce.eventif.entity.Event;
import br.edu.ifce.eventif.entity.EventStatus;
import br.edu.ifce.eventif.entity.User;
import br.edu.ifce.eventif.repository.ActivityRepository;
import br.edu.ifce.eventif.repository.CategoryRepository;
import br.edu.ifce.eventif.repository.EventRepository;
import br.edu.ifce.eventif.repository.UserRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventService {
    private final EventRepository events;
    private final ActivityRepository activities;
    private final CategoryRepository categories;
    private final UserRepository users;

    public EventService(EventRepository events, ActivityRepository activities, CategoryRepository categories, UserRepository users) {
        this.events = events; this.activities = activities; this.categories = categories; this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Event> list(String email) {
        return events.findByOrganizerId(organizer(email).getId());
    }

    @Transactional(readOnly = true)
    public Event findOwned(Long id, String email) {
        Event event = events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return checkOwner(event, email);
    }

    @Transactional(readOnly = true)
    public Event findEditable(Long id, String email) {
        Event event = findOwned(id, email);
        ensureEditable(event);
        return event;
    }

    @Transactional
    public void create(EventForm form, String email) {
        validateDates(form);
        events.save(
            new Event(
                form.getTitle().strip(), 
                form.getDescription().strip(), 
                form.getLocation().strip(),
                form.getStartDate(), 
                form.getEndDate(), 
                category(form.getCategoryId()), 
                organizer(email)
            )
        );
    }

    @Transactional
    public void update(Long id, EventForm form, String email) {
        Event event = findOwned(id, email);
        ensureEditable(event);
        validateDates(form);
        boolean outside = activities.findByEventId(id).stream().anyMatch(activity ->
                activity.getStartDateTime().toLocalDate().isBefore(form.getStartDate()) ||
                activity.getEndDateTime().toLocalDate().isAfter(form.getEndDate()));
        if (outside) throw new IllegalArgumentException("As atividades devem permanecer dentro do período do evento.");
        event.setTitle(form.getTitle().strip()); 
        event.setDescription(form.getDescription().strip());
        event.setLocation(form.getLocation().strip()); 
        event.setStartDate(form.getStartDate());
        event.setEndDate(form.getEndDate()); 
        event.setCategory(category(form.getCategoryId()));
    }

    @Transactional
    public void publish(Long id, String email) {
        Event event = findOwned(id, email);
        if (event.getStatus() != EventStatus.DRAFT) throw new IllegalArgumentException("Somente eventos em rascunho podem ser publicados.");
        if (activities.countByEventId(id) == 0) throw new IllegalArgumentException("Inclua pelo menos uma atividade antes de publicar.");
        event.setStatus(EventStatus.PUBLISHED);
    }

    @Transactional
    public void close(Long id, String email) {
        Event event = findOwned(id, email);
        if (event.getStatus() != EventStatus.PUBLISHED) throw new IllegalArgumentException("Somente eventos publicados podem ser encerrados.");
        event.setStatus(EventStatus.CLOSED);
    }

    @Transactional
    public void delete(Long id, String email) {
        Event event = findOwned(id, email);
        ensureEditable(event);
        if (activities.countByEventId(id) > 0) throw new IllegalArgumentException("Não é possível excluir o evento porque há atividades associadas.");
        try { 
            events.delete(event); 
            events.flush(); 
        }
        catch (DataIntegrityViolationException ex) { throw new IllegalArgumentException("Não é possível excluir o evento porque há dependências associadas."); }
    }

    private Event checkOwner(Event event, String email) {
        if (!event.getOrganizer().getEmail().equals(email)) throw new AccessDeniedException("Evento de outro organizador.");
        return event;
    }
    private void ensureEditable(Event event) {
        if (event.getStatus() == EventStatus.CLOSED) throw new IllegalArgumentException("Eventos encerrados não podem ser alterados ou excluídos.");
    }
    private void validateDates(EventForm form) {
        if (form.getStartDate() == null || form.getEndDate() == null || form.getEndDate().isBefore(form.getStartDate()))
            throw new IllegalArgumentException("A data de término não pode ser anterior à data de início.");
    }
    private User organizer(String email) {
        return users.findByEmail(email).orElseThrow(() -> new AccessDeniedException("Usuário não encontrado."));
    }
    private Category category(Long id) {
        return categories.findById(id).orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
    }
}
