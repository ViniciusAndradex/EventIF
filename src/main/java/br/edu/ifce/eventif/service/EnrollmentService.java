package br.edu.ifce.eventif.service;

import br.edu.ifce.eventif.entity.Activity;
import br.edu.ifce.eventif.entity.Enrollment;
import br.edu.ifce.eventif.entity.Event;
import br.edu.ifce.eventif.entity.EventStatus;
import br.edu.ifce.eventif.entity.User;
import br.edu.ifce.eventif.repository.ActivityRepository;
import br.edu.ifce.eventif.repository.EnrollmentRepository;
import br.edu.ifce.eventif.repository.EventRepository;
import br.edu.ifce.eventif.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EnrollmentService {
    private final EventRepository events;
    private final ActivityRepository activities;
    private final EnrollmentRepository enrollments;
    private final UserRepository users;
    private final Clock clock;

    public EnrollmentService(EventRepository events, ActivityRepository activities,
            EnrollmentRepository enrollments, UserRepository users, Clock clock) {
        this.events = events;
        this.activities = activities;
        this.enrollments = enrollments;
        this.users = users;
        this.clock = clock;
    }

    @Transactional
    public void enroll(Long activityId, String email) {
        User participant = participant(email);
        Activity activity = activities.findById(activityId).orElseThrow(this::notFound);
        Event event = activity.getEvent();
        LocalDateTime now = LocalDateTime.now(clock);
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Inscrições são permitidas apenas em eventos publicados.");
        }
        if (!now.isBefore(activity.getEndDateTime())) {
            throw new IllegalArgumentException("A atividade já terminou.");
        }
        if (enrollments.existsByUserIdAndActivityId(participant.getId(), activityId)) {
            throw new IllegalArgumentException("Você já está inscrito nesta atividade.");
        }
        if (enrollments.countByActivityId(activityId) >= activity.getCapacity()) {
            throw new IllegalArgumentException("Não há vagas disponíveis nesta atividade.");
        }
        enrollments.save(new Enrollment(now, false, participant, activity));
    }

    @Transactional
    public void cancel(Long enrollmentId, String email) {
        User participant = participant(email);
        Enrollment existing = enrollments.findById(enrollmentId).orElseThrow(this::notFound);
        if (!existing.getUser().getId().equals(participant.getId())) {
            throw new AccessDeniedException("Inscrição de outro participante.");
        }
        Activity activity = activities.findById(existing.getActivity().getId()).orElseThrow(this::notFound);
        Enrollment enrollment = enrollments.findByUserIdAndActivityId(participant.getId(), activity.getId())
                .filter(item -> item.getId().equals(enrollmentId)).orElseThrow(this::notFound);
        if (activity.getEvent().getStatus() == EventStatus.CLOSED) {
            throw new IllegalArgumentException("Inscrições de eventos encerrados não podem ser canceladas.");
        }
        if (!LocalDateTime.now(clock).isBefore(activity.getStartDateTime())) {
            throw new IllegalArgumentException("O cancelamento só é permitido antes do início da atividade.");
        }
        enrollments.delete(enrollment);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> listMine(String email) {
        User participant = participant(email);
        List<Enrollment> result = enrollments.findByUserId(participant.getId());
        result.forEach(enrollment -> enrollment.getActivity().getEvent().getTitle());
        return result;
    }

    @Transactional(readOnly = true)
    public Roster roster(Long eventId, Long activityId, String email) {
        Event event = ownedEvent(eventId, email);
        Activity activity = activityForEvent(activityId, eventId);
        List<Enrollment> result = enrollments.findByActivityId(activityId);
        result.forEach(enrollment -> enrollment.getUser().getName());
        LocalDateTime now = LocalDateTime.now(clock);
        boolean canMark = event.getStatus() != EventStatus.CLOSED
                && !now.isBefore(activity.getStartDateTime()) && now.isBefore(activity.getEndDateTime());
        return new Roster(event, activity, result, canMark);
    }

    @Transactional
    public void markPresent(Long eventId, Long activityId, Long enrollmentId, String email) {
        Event event = events.findById(eventId).orElseThrow(this::notFound);
        checkOwner(event, email);
        Activity activity = activities.findById(activityId).orElseThrow(this::notFound);
        if (!activity.getEvent().getId().equals(eventId)) throw notFound();
        Enrollment enrollment = enrollments.findById(enrollmentId).orElseThrow(this::notFound);
        if (!enrollment.getActivity().getId().equals(activityId)) throw notFound();
        if (event.getStatus() == EventStatus.CLOSED) {
            throw new IllegalArgumentException("Eventos encerrados não permitem alterações de presença.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (now.isBefore(activity.getStartDateTime()) || !now.isBefore(activity.getEndDateTime())) {
            throw new IllegalArgumentException("A presença só pode ser marcada durante a atividade.");
        }
        enrollment.setPresent(true);
    }

    private User participant(String email) {
        return users.findByEmail(email)
                .filter(user -> user.isActive() && user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_PARTICIPANTE")))
                .orElseThrow(() -> new AccessDeniedException("Apenas participantes podem acessar inscrições."));
    }

    private Event ownedEvent(Long id, String email) {
        Event event = events.findById(id).orElseThrow(this::notFound);
        checkOwner(event, email);
        return event;
    }

    private void checkOwner(Event event, String email) {
        if (!event.getOrganizer().getEmail().equals(email)) {
            throw new AccessDeniedException("Evento de outro organizador.");
        }
    }

    private Activity activityForEvent(Long activityId, Long eventId) {
        Activity activity = activities.findById(activityId).orElseThrow(this::notFound);
        if (!activity.getEvent().getId().equals(eventId)) throw notFound();
        return activity;
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public record Roster(Event event, Activity activity, List<Enrollment> enrollments, boolean canMark) {}
}
