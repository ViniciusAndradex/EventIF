package br.edu.ifce.eventif.service;

import br.edu.ifce.eventif.entity.*;
import br.edu.ifce.eventif.repository.*;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dados de demonstração para a gravação do vídeo. Só roda com DEMO_DATA=true.
 * É idempotente e, a cada subida, realinha os horários do evento aberto para que
 * a "Oficina de Spring Boot" esteja acontecendo no momento da gravação.
 */
@Service
public class DemoDataService {
    private static final Logger log = LoggerFactory.getLogger(DemoDataService.class);
    private static final String OPEN_EVENT = "Semana de Tecnologia IFCE";
    private static final String CLOSED_EVENT = "Jornada de Educação 2026";
    private static final String OTHER_EVENT = "Congresso de Computação";

    private final UserRepository users;
    private final RoleRepository roles;
    private final CategoryRepository categories;
    private final EventRepository events;
    private final ActivityRepository activities;
    private final EnrollmentRepository enrollments;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final String password;
    private final String organizerEmail;
    private final String participantEmail;

    public DemoDataService(UserRepository users, RoleRepository roles, CategoryRepository categories,
                           EventRepository events, ActivityRepository activities, EnrollmentRepository enrollments,
                           PasswordEncoder passwordEncoder, Clock clock,
                           @Value("${SEED_PASSWORD:}") String password,
                           @Value("${ORGANIZER_EMAIL:}") String organizerEmail,
                           @Value("${PARTICIPANT_EMAIL:}") String participantEmail) {
        this.users = users; this.roles = roles; this.categories = categories; this.events = events;
        this.activities = activities; this.enrollments = enrollments; this.passwordEncoder = passwordEncoder;
        this.clock = clock; this.password = password;
        this.organizerEmail = organizerEmail; this.participantEmail = participantEmail;
    }

    @Transactional
    public void initialize() {
        Role participantRole = roles.findByName("ROLE_PARTICIPANTE").orElseThrow();
        Role organizerRole = roles.findByName("ROLE_ORGANIZADOR").orElseThrow();
        User organizer = users.findByEmail(organizerEmail).orElseThrow();
        User participant = users.findByEmail(participantEmail).orElseThrow();
        User secondOrganizer = user("Segundo Organizador", "organizador2@test.com", organizerRole);
        User ana = user("Ana Souza", "ana.souza@test.com", participantRole);
        User bruno = user("Bruno Lima", "bruno.lima@test.com", participantRole);
        User carla = user("Carla Mendes", "carla.mendes@test.com", participantRole);
        Category technology = categories.findFirstByName("Tecnologia").orElseThrow();
        Category education = categories.findFirstByName("Educação").orElseThrow();

        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
        LocalDate today = now.toLocalDate();

        // Evento publicado do organizador: presença, vagas esgotadas e inscrição/cancelamento.
        Event open = event(OPEN_EVENT, "Semana com oficinas, palestras e minicursos de tecnologia.",
                "IFCE – Campus Fortaleza", technology, organizer, EventStatus.PUBLISHED);
        open.setStartDate(today.minusDays(1));
        open.setEndDate(today.plusDays(7));
        Activity workshop = activity(open, "Oficina de Spring Boot", ActivityType.WORKSHOP, "Prof. Corneli",
                now.minusMinutes(30), now.plusHours(3), 20);
        Activity fullLecture = activity(open, "Palestra de Segurança Web", ActivityType.LECTURE, "Equipe EventIF",
                today.plusDays(1).atTime(9, 0), today.plusDays(1).atTime(11, 0), 1);
        activity(open, "Minicurso de JPA", ActivityType.MINICOURSE, "Equipe EventIF",
                today.plusDays(2).atTime(14, 0), today.plusDays(2).atTime(18, 0), 30);
        enroll(participant, workshop, false);
        enroll(ana, workshop, false);
        enroll(bruno, workshop, false);
        enroll(carla, workshop, false);
        enroll(ana, fullLecture, false);

        // Evento encerrado com presença do participante: certificado (HTML e PDF).
        Event closed = event(CLOSED_EVENT, "Jornada encerrada, usada para emitir certificados.",
                "IFCE – Campus Fortaleza", education, organizer, EventStatus.CLOSED);
        if (activities.countByEventId(closed.getId()) == 0) {
            closed.setStartDate(today.minusDays(10));
            closed.setEndDate(today.minusDays(9));
            Activity a1 = activity(closed, "Palestra de Abertura", ActivityType.LECTURE, "Equipe EventIF",
                    today.minusDays(10).atTime(9, 0), today.minusDays(10).atTime(11, 0), 50);
            Activity a2 = activity(closed, "Oficina de Metodologias Ativas", ActivityType.WORKSHOP, "Equipe EventIF",
                    today.minusDays(9).atTime(14, 0), today.minusDays(9).atTime(17, 30), 50);
            enroll(participant, a1, true);
            enroll(participant, a2, true);
            enroll(ana, a1, false);
        }

        // Evento do segundo organizador: o primeiro organizador não pode alterá-lo (403), e vice-versa.
        Event other = event(OTHER_EVENT, "Evento de outro organizador, usado na demonstração de segurança.",
                "Auditório Central", technology, secondOrganizer, EventStatus.PUBLISHED);
        other.setStartDate(today.plusDays(10));
        other.setEndDate(today.plusDays(11));
        activity(other, "Palestra de Abertura do Congresso", ActivityType.LECTURE, "Segundo Organizador",
                today.plusDays(10).atTime(9, 0), today.plusDays(10).atTime(10, 0), 40);

        log.info("[DEMO] Dados do vídeo prontos. Senha de todos: SEED_PASSWORD.");
        log.info("[DEMO] '{}' (organizer, PUBLICADO) id={} | '{}' (ENCERRADO, certificado) id={} | '{}' (organizador2) id={}",
                OPEN_EVENT, open.getId(), CLOSED_EVENT, closed.getId(), OTHER_EVENT, other.getId());
        log.info("[DEMO] Teste do 403: entre como organizador2@test.com e abra /organizador/eventos/{}/editar", open.getId());
    }

    private User user(String name, String email, Role role) {
        return users.findByEmail(email).orElseGet(() ->
                users.save(new User(name, email, passwordEncoder.encode(password), true, role)));
    }

    private Event event(String title, String description, String location, Category category, User organizer,
                        EventStatus status) {
        return events.findFirstByTitleAndOrganizerId(title, organizer.getId()).orElseGet(() -> {
            LocalDate today = LocalDate.now(clock);
            Event event = new Event(title, description, location, today, today, category, organizer);
            event.setStatus(status);
            return events.save(event);
        });
    }

    /** Cria a atividade ou realinha os horários dela (eventos não encerrados). */
    private Activity activity(Event event, String title, ActivityType type, String instructor,
                              LocalDateTime start, LocalDateTime end, int capacity) {
        int minutes = (int) Duration.between(start, end).toMinutes();
        return activities.findFirstByTitleAndEventId(title, event.getId()).map(existing -> {
            if (event.getStatus() != EventStatus.CLOSED) {
                existing.setStartDateTime(start);
                existing.setEndDateTime(end);
                existing.setWorkloadMinutes(minutes);
            }
            return existing;
        }).orElseGet(() -> activities.save(new Activity(title, type, instructor, start, end, minutes, capacity, event)));
    }

    private void enroll(User user, Activity activity, boolean present) {
        if (!enrollments.existsByUserIdAndActivityId(user.getId(), activity.getId())) {
            enrollments.save(new Enrollment(LocalDateTime.now(clock), present, user, activity));
        }
    }
}
