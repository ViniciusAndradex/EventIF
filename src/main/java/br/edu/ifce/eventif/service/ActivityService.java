package br.edu.ifce.eventif.service;

import br.edu.ifce.eventif.dto.ActivityForm;
import br.edu.ifce.eventif.entity.Activity;
import br.edu.ifce.eventif.entity.Event;
import br.edu.ifce.eventif.entity.EventStatus;
import br.edu.ifce.eventif.repository.ActivityRepository;
import br.edu.ifce.eventif.repository.EnrollmentRepository;
import br.edu.ifce.eventif.repository.EventRepository;
import java.util.List;
import java.time.Duration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ActivityService {
    private final EventRepository events;
    private final ActivityRepository activities;
    private final EnrollmentRepository enrollments;

    public ActivityService(EventRepository events, ActivityRepository activities, EnrollmentRepository enrollments) {
        this.events = events;
        this.activities = activities;
        this.enrollments = enrollments;
    }

    @Transactional(readOnly = true)
    public Event event(Long id, String email) {
        return owned(events.findById(id).orElseThrow(() -> notFound()), email);
    }

    @Transactional(readOnly = true)
    public List<Activity> list(Long eventId, String email) {
        event(eventId, email);
        return activities.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public Activity find(Long eventId, Long activityId, String email) {
        event(eventId, email);
        return activityForEvent(activities.findById(activityId).orElseThrow(() -> notFound()), eventId, email);
    }

    @Transactional(readOnly = true)
    public Event editableEvent(Long eventId, String email) {
        Event event = event(eventId, email);
        editable(event);
        return event;
    }

    @Transactional(readOnly = true)
    public ActivityForm editForm(Long eventId, Long activityId, String email) {
        Activity activity = find(eventId, activityId, email);
        editableEvent(eventId, email);
        ActivityForm form = new ActivityForm();
        form.setTitle(activity.getTitle());
        form.setType(activity.getType());
        form.setInstructor(activity.getInstructor());
        form.setStartDateTime(activity.getStartDateTime());
        form.setEndDateTime(activity.getEndDateTime());
        form.setCapacity(activity.getCapacity());
        return form;
    }

    @Transactional
    public void create(Long eventId, ActivityForm form, String email) {
        Event event = ownedEvent(eventId, email);
        editable(event);
        int workload = validate(form, event);
        activities.save(new Activity(form.getTitle().strip(), form.getType(), form.getInstructor().strip(),
                form.getStartDateTime(), form.getEndDateTime(), workload, form.getCapacity(), event));
    }

    @Transactional
    public void update(Long eventId, Long activityId, ActivityForm form, String email) {
        Event event = ownedEvent(eventId, email);
        Activity activity = ownedActivity(activityId, eventId, email);
        editable(event);
        int workload = validate(form, event);
        if (form.getCapacity() < enrollments.countByActivityId(activityId))
            throw new IllegalArgumentException("O número de vagas não pode ser menor que o número de inscritos.");
        activity.setTitle(form.getTitle().strip());
        activity.setType(form.getType());
        activity.setInstructor(form.getInstructor().strip());
        activity.setStartDateTime(form.getStartDateTime());
        activity.setEndDateTime(form.getEndDateTime());
        activity.setWorkloadMinutes(workload);
        activity.setCapacity(form.getCapacity());
    }

    @Transactional
    public void delete(Long eventId, Long activityId, String email) {
        Event event = ownedEvent(eventId, email);
        Activity activity = ownedActivity(activityId, eventId, email);
        editable(event);
        if (enrollments.countByActivityId(activityId) > 0)
            throw new IllegalArgumentException("Não é possível excluir uma atividade com inscrições.");
        if (event.getStatus() == EventStatus.PUBLISHED && activities.countByEventId(eventId) <= 1)
            throw new IllegalArgumentException("Um evento publicado deve manter pelo menos uma atividade.");
        try {
            activities.delete(activity);
            activities.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Não é possível excluir uma atividade com inscrições.");
        }
    }

    private Event ownedEvent(Long id, String email) {
        return owned(events.findById(id).orElseThrow(() -> notFound()), email);
    }

    private Event owned(Event event, String email) {
        if (!event.getOrganizer().getEmail().equals(email))
            throw new AccessDeniedException("Evento de outro organizador.");
        return event;
    }

    private Activity ownedActivity(Long id, Long eventId, String email) {
        return activityForEvent(activities.findById(id).orElseThrow(() -> notFound()), eventId, email);
    }

    private Activity activityForEvent(Activity activity, Long eventId, String email) {
        owned(activity.getEvent(), email);
        if (!activity.getEvent().getId().equals(eventId))
            throw notFound();
        return activity;
    }

    private void editable(Event event) {
        if (event.getStatus() == EventStatus.CLOSED)
            throw new IllegalArgumentException("Atividades de eventos encerrados não podem ser alteradas.");
    }

    private int validate(ActivityForm form, Event event) {
        if (form.getStartDateTime() == null || form.getEndDateTime() == null
                || !form.getEndDateTime().isAfter(form.getStartDateTime()))
            throw new IllegalArgumentException("O término deve ser posterior ao início da atividade.");
        if (form.getStartDateTime().toLocalDate().isBefore(event.getStartDate())
                || form.getEndDateTime().toLocalDate().isAfter(event.getEndDate()))
            throw new IllegalArgumentException("A atividade deve estar dentro das datas do evento.");
        Duration duration = Duration.between(form.getStartDateTime(), form.getEndDateTime());
        long total = duration.toMinutes();
        if (duration.getSeconds() % 60 != 0 || duration.getNano() != 0 || total <= 0 || total > Integer.MAX_VALUE)
            throw new IllegalArgumentException(
                    "A duração deve ter pelo menos um minuto inteiro e estar dentro do limite permitido.");
        return (int) total;
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
