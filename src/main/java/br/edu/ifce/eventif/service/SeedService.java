package br.edu.ifce.eventif.service;

import br.edu.ifce.eventif.entity.*;
import br.edu.ifce.eventif.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeedService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final CategoryRepository categories;
    private final EventRepository events;
    private final ActivityRepository activities;
    private final PasswordEncoder passwordEncoder;
    private final String seedPassword;

    public SeedService(UserRepository users, RoleRepository roles, CategoryRepository categories,
                       EventRepository events, ActivityRepository activities, PasswordEncoder passwordEncoder,
                       @Value("${SEED_PASSWORD:}") String seedPassword) {
        this.users = users; this.roles = roles; this.categories = categories;
        this.events = events; this.activities = activities; this.passwordEncoder = passwordEncoder;
        this.seedPassword = seedPassword;
    }

    @Transactional
    public void initialize() {
        Role participant = role("ROLE_PARTICIPANTE");
        Role organizerRole = role("ROLE_ORGANIZADOR");
        Role admin = role("ROLE_ADMIN");
        user("Participante", "participante@eventif.local", participant);
        User organizer = user("Organizador", "organizador@eventif.local", organizerRole);
        user("Administrador", "admin@eventif.local", admin);
        Category technology = category("Tecnologia");
        Category education = category("Educação");
        event("Encontro de Tecnologia", technology, organizer, LocalDate.of(2030, 5, 10),
                "Introdução à tecnologia", ActivityType.LECTURE);
        event("Encontro de Educação", education, organizer, LocalDate.of(2030, 6, 10),
                "Oficina de educação", ActivityType.WORKSHOP);
    }

    private Role role(String name) {
        return roles.findByName(name).orElseGet(() -> roles.save(new Role(name)));
    }

    private User user(String name, String email, Role role) {
        return users.findByEmail(email).orElseGet(() -> {
            if (seedPassword.isBlank()) {
                throw new IllegalStateException("Defina SEED_PASSWORD para criar os usuários iniciais.");
            }
            return users.save(new User(name, email, passwordEncoder.encode(seedPassword), true, role));
        });
    }

    private Category category(String name) {
        return categories.findFirstByName(name).orElseGet(() -> categories.save(new Category(name)));
    }

    private void event(String title, Category category, User organizer, LocalDate date,
                       String activityTitle, ActivityType type) {
        Event event = events.findFirstByTitleAndOrganizerId(title, organizer.getId())
                .orElseGet(() -> events.save(new Event(title, "Evento acadêmico de exemplo.", "IFCE",
                        date, date, category, organizer)));
        if (event.getStatus() != EventStatus.CLOSED
                && activities.findFirstByTitleAndEventId(activityTitle, event.getId()).isEmpty()) {
            LocalDateTime start = date.atTime(9, 0);
            activities.save(new Activity(activityTitle, type, "Equipe EventIF", start, start.plusHours(2),
                    120, 30, event));
        }
    }
}
