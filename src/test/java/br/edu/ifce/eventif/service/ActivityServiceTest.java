package br.edu.ifce.eventif.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.edu.ifce.eventif.dto.ActivityForm;
import br.edu.ifce.eventif.entity.*;
import br.edu.ifce.eventif.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {
    static final String END_AFTER_START = "O término deve ser posterior ao início da atividade.";
    static final String WITHIN_EVENT = "A atividade deve estar dentro das datas do evento.";
    @Mock EventRepository events;
    @Mock ActivityRepository activities;
    @Mock EnrollmentRepository enrollments;
    @Mock Event event;
    @Mock User organizer;
    ActivityService service;
    @BeforeEach void setUp() {
        service = new ActivityService(events, activities, enrollments);
        when(events.findById(1L)).thenReturn(Optional.of(event));
        when(event.getOrganizer()).thenReturn(organizer);
        when(organizer.getEmail()).thenReturn("dono");
        when(event.getStatus()).thenReturn(EventStatus.DRAFT);
    }
    private ActivityForm form(LocalDateTime start, LocalDateTime end) {
        ActivityForm form = new ActivityForm();
        form.setTitle("Atividade"); form.setInstructor("Instrutor"); form.setType(ActivityType.LECTURE);
        form.setStartDateTime(start); form.setEndDateTime(end); form.setCapacity(10);
        return form;
    }
    @Test void endMustBeAfterStart() {
        LocalDateTime date = LocalDateTime.of(2030, 5, 10, 9, 0);
        var error = assertThrows(IllegalArgumentException.class, () -> service.create(1L, form(date, date), "dono"));
        assertEquals(END_AFTER_START, error.getMessage());
        verify(activities, never()).save(any());
    }
    @Test void endBeforeStartIsRejected() {
        var error = assertThrows(IllegalArgumentException.class, () -> service.create(1L,
                form(LocalDateTime.of(2030, 5, 10, 10, 0), LocalDateTime.of(2030, 5, 10, 9, 0)), "dono"));
        assertEquals(END_AFTER_START, error.getMessage());
        verify(activities, never()).save(any());
    }
    @Test void activityMustRemainWithinEventDates() {
        when(event.getStartDate()).thenReturn(LocalDate.of(2030, 5, 10));
        var error = assertThrows(IllegalArgumentException.class, () -> service.create(1L,
                form(LocalDateTime.of(2030, 5, 9, 23, 0), LocalDateTime.of(2030, 5, 10, 1, 0)), "dono"));
        assertEquals(WITHIN_EVENT, error.getMessage());
        verify(activities, never()).save(any());
    }
    @Test void activityMustNotEndAfterEventEndDate() {
        when(event.getStartDate()).thenReturn(LocalDate.of(2030, 5, 10));
        when(event.getEndDate()).thenReturn(LocalDate.of(2030, 5, 11));
        var error = assertThrows(IllegalArgumentException.class, () -> service.create(1L,
                form(LocalDateTime.of(2030, 5, 11, 23, 0), LocalDateTime.of(2030, 5, 12, 1, 0)), "dono"));
        assertEquals(WITHIN_EVENT, error.getMessage());
        verify(activities, never()).save(any());
    }
    @Test void activityMayUseFirstAndLastEventDates() {
        when(event.getStartDate()).thenReturn(LocalDate.of(2030, 5, 10));
        when(event.getEndDate()).thenReturn(LocalDate.of(2030, 5, 11));
        assertDoesNotThrow(() -> service.create(1L,
                form(LocalDateTime.of(2030, 5, 10, 9, 0), LocalDateTime.of(2030, 5, 11, 10, 0)), "dono"));
        var saved = org.mockito.ArgumentCaptor.forClass(Activity.class);
        verify(activities).save(saved.capture());
        assertEquals(1500, saved.getValue().getWorkloadMinutes());
    }
}
