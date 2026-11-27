package br.edu.ifce.eventif.service;

import br.edu.ifce.eventif.dto.RegistrationForm;
import br.edu.ifce.eventif.entity.User;
import br.edu.ifce.eventif.repository.RoleRepository;
import br.edu.ifce.eventif.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, RoleRepository roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional
    public void register(RegistrationForm form) {
        if (users.existsByEmail(form.getEmail()))
            throw new IllegalArgumentException("Este email já está cadastrado.");
        var role = roles.findByName("ROLE_PARTICIPANTE").orElseThrow();
        users.saveAndFlush(new User(form.getName(), form.getEmail(), encoder.encode(form.getPassword()), true, role));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = users.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword()).disabled(!user.isActive())
                .authorities(user.getRoles().stream().map(role -> role.getName()).toArray(String[]::new)).build();
    }
}
