package br.edu.ifce.eventif.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ifce.eventif.entity.Activity;
import br.edu.ifce.eventif.entity.Event;
import br.edu.ifce.eventif.entity.EventStatus;
import br.edu.ifce.eventif.entity.Role;
import br.edu.ifce.eventif.entity.User;
import br.edu.ifce.eventif.repository.ActivityRepository;
import br.edu.ifce.eventif.repository.EnrollmentRepository;
import br.edu.ifce.eventif.repository.EventRepository;
import br.edu.ifce.eventif.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EnrollmentServiceTest {
    @Test
    void rn04RejectsDuplicateEnrollment() {
        EventRepository events = mock(EventRepository.class);
        ActivityRepository activities = mock(ActivityRepository.class);
        EnrollmentRepository enrollments = mock(EnrollmentRepository.class);
        UserRepository users = mock(UserRepository.class);
        User participant = mock(User.class);
        Role role = mock(Role.class);
        Event event = mock(Event.class);
        Activity activity = mock(Activity.class);
        Clock clock = Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC);
        EnrollmentService service = new EnrollmentService(events, activities, enrollments, users, clock);

        when(users.findByEmail("aluno@example.com")).thenReturn(Optional.of(participant));
        when(participant.isActive()).thenReturn(true);
        when(participant.getRoles()).thenReturn(Set.of(role));
        when(role.getName()).thenReturn("ROLE_PARTICIPANTE");
        when(participant.getId()).thenReturn(1L);
        when(activity.getEvent()).thenReturn(event);
        when(activities.findById(2L)).thenReturn(Optional.of(activity));
        when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
        when(activity.getEndDateTime()).thenReturn(LocalDateTime.of(2030, 1, 1, 12, 0));
        when(enrollments.existsByUserIdAndActivityId(1L, 2L)).thenReturn(true);

        var error = assertThrows(IllegalArgumentException.class, () -> service.enroll(2L, "aluno@example.com"));
        assertEquals("Você já está inscrito nesta atividade.", error.getMessage());
        verify(enrollments, never()).save(any());
    }
}
