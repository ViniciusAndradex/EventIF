package br.edu.ifce.eventif.service;

import br.edu.ifce.eventif.entity.EventStatus;
import br.edu.ifce.eventif.entity.User;
import br.edu.ifce.eventif.repository.EventRepository;
import br.edu.ifce.eventif.repository.RoleRepository;
import br.edu.ifce.eventif.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdministrationService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final EventRepository events;

    public UserAdministrationService(UserRepository users, RoleRepository roles, EventRepository events) {
        this.users = users;
        this.roles = roles;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return users.findAllByOrderByNameAscIdAsc();
    }

    @Transactional
    public void grantOrganizer(Long id) {
        User user = findModifiableUser(id);
        if (hasRole(user, "ROLE_ORGANIZADOR")) {
            throw new IllegalArgumentException("O usuário já é organizador.");
        }
        user.setRole(roles.findByName("ROLE_ORGANIZADOR").orElseThrow());
    }

    @Transactional
    public void revokeOrganizer(Long id) {
        User user = findModifiableUser(id);
        if (!hasRole(user, "ROLE_ORGANIZADOR")) {
            throw new IllegalArgumentException("O usuário não é organizador.");
        }
        if (events.existsByOrganizerIdAndStatus(id, EventStatus.PUBLISHED)) {
            throw new IllegalArgumentException("Não é possível retirar o papel: o usuário possui evento publicado.");
        }
        events.findByOrganizerIdAndStatus(id, EventStatus.DRAFT)
                .forEach(event -> event.setStatus(EventStatus.CLOSED));
        user.setRole(roles.findByName("ROLE_PARTICIPANTE").orElseThrow());
    }

    private User findModifiableUser(Long id) {
        User user = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (hasRole(user, "ROLE_ADMIN")) {
            throw new IllegalArgumentException("Não é permitido alterar o papel de um administrador.");
        }
        return user;
    }

    private boolean hasRole(User user, String role) {
        return user.getRoles().stream().anyMatch(item -> item.getName().equals(role));
    }
}
