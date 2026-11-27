package br.edu.ifce.eventif.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ifce.eventif.dto.EventForm;
import br.edu.ifce.eventif.entity.Activity;
import br.edu.ifce.eventif.entity.Category;
import br.edu.ifce.eventif.entity.Event;
import br.edu.ifce.eventif.entity.EventStatus;
import br.edu.ifce.eventif.entity.User;
import br.edu.ifce.eventif.repository.ActivityRepository;
import br.edu.ifce.eventif.repository.CategoryRepository;
import br.edu.ifce.eventif.repository.EventRepository;
import br.edu.ifce.eventif.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock
    EventRepository events;
    @Mock
    ActivityRepository activities;
    @Mock
    CategoryRepository categories;
    @Mock
    UserRepository users;
    @Mock
    User organizer;
    @Mock
    Category category;
    @Mock
    Event event;
    @Mock
    Activity activity;
    EventService service;

    @BeforeEach
    void setUp() {
        service = new EventService(events, activities, categories, users);
    }

    private EventForm form(LocalDate start, LocalDate end) {
        EventForm form = new EventForm();
        form.setTitle("Evento");
        form.setDescription("Descrição");
        form.setLocation("Local");
        form.setStartDate(start);
        form.setEndDate(end);
        form.setCategoryId(1L);
        return form;
    }

    @Test
    void rejectsEndBeforeStart() {
        EventForm form = form(LocalDate.of(2030, 5, 2), LocalDate.of(2030, 5, 1));
        var error = assertThrows(IllegalArgumentException.class, () -> service.create(form, "dono"));
        assertEquals("A data de término não pode ser anterior à data de início.", error.getMessage());
        verify(events, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void acceptsSameStartAndEndDay() {
        EventForm form = form(LocalDate.of(2030, 5, 1), LocalDate.of(2030, 5, 1));
        when(categories.findById(1L)).thenReturn(Optional.of(category));
        when(users.findByEmail("dono")).thenReturn(Optional.of(organizer));
        assertDoesNotThrow(() -> service.create(form, "dono"));
        verify(events).save(org.mockito.ArgumentMatchers.any(Event.class));
    }

    @Test
    void rejectsDatesThatExcludeExistingActivity() {
        EventForm form = form(LocalDate.of(2030, 5, 2), LocalDate.of(2030, 5, 3));
        when(events.findById(1L)).thenReturn(Optional.of(event));
        when(event.getOrganizer()).thenReturn(organizer);
        when(organizer.getEmail()).thenReturn("dono");
        when(event.getStatus()).thenReturn(EventStatus.DRAFT);
        when(activities.findByEventId(1L)).thenReturn(List.of(activity));
        when(activity.getStartDateTime()).thenReturn(LocalDateTime.of(2030, 5, 1, 9, 0));
        var error = assertThrows(IllegalArgumentException.class, () -> service.update(1L, form, "dono"));
        assertEquals("As atividades devem permanecer dentro do período do evento.", error.getMessage());
        verify(event, never()).setStartDate(org.mockito.ArgumentMatchers.any());
    }
}
