package br.edu.ifce.eventif.service;

import br.edu.ifce.eventif.entity.Activity;
import br.edu.ifce.eventif.entity.Event;
import br.edu.ifce.eventif.entity.EventStatus;
import br.edu.ifce.eventif.entity.User;
import br.edu.ifce.eventif.repository.ActivityRepository;
import br.edu.ifce.eventif.repository.EnrollmentRepository;
import br.edu.ifce.eventif.repository.EventRepository;
import br.edu.ifce.eventif.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
    private static final int PAGE_SIZE = 9;
    private final EventRepository events;
    private final ActivityRepository activities;
    private final EnrollmentRepository enrollments;
    private final UserRepository users;

    public CatalogService(EventRepository events, ActivityRepository activities, EnrollmentRepository enrollments,
            UserRepository users) {
        this.events = events;
        this.activities = activities;
        this.enrollments = enrollments;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Page<Event> list(Long categoryId, int page) {
        var pageable = PageRequest.of(Math.min(Math.max(page, 0), Integer.MAX_VALUE / PAGE_SIZE), PAGE_SIZE);
        Page<Event> result = categoryId == null
                ? events.findByStatusOrderByStartDateAscIdAsc(EventStatus.PUBLISHED, pageable)
                : events.findByStatusAndCategoryIdOrderByStartDateAscIdAsc(EventStatus.PUBLISHED, categoryId, pageable);
        result.forEach(event -> event.getCategory().getName());
        return result;
    }

    @Transactional(readOnly = true)
    public EventDetail detail(Long id, String email) {
        Event event = events.findById(id).filter(candidate -> candidate.getStatus() == EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        event.getCategory().getName();
        event.getOrganizer().getName();
        User participant = email == null ? null
                : users.findByEmail(email).filter(user -> user.isActive()
                        && user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_PARTICIPANTE")))
                        .orElse(null);
        List<ActivityDetail> details = activities.findByEventId(id).stream().map(activity -> {
            long count = enrollments.countByActivityId(activity.getId());
            boolean enrolled = participant != null
                    && enrollments.existsByUserIdAndActivityId(participant.getId(), activity.getId());
            int minutes = activity.getWorkloadMinutes();
            String workload = minutes / 60 + "h" + String.format("%02d", minutes % 60);
            return new ActivityDetail(activity, workload, count, activity.getCapacity() - count, enrolled);
        }).toList();
        return new EventDetail(event, details, participant != null);
    }

    public record EventDetail(Event event, List<ActivityDetail> activities, boolean participant) {
    }

    public record ActivityDetail(Activity activity, String workload, long enrolledCount, long remaining,
            boolean enrolled) {
    }
}
