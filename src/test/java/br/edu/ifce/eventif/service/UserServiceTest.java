package br.edu.ifce.eventif.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ifce.eventif.dto.RegistrationForm;
import br.edu.ifce.eventif.repository.RoleRepository;
import br.edu.ifce.eventif.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {
    @Test
    void rn01RejectsDuplicateEmail() {
        UserRepository users = mock(UserRepository.class);
        UserService service = new UserService(users, mock(RoleRepository.class), mock(PasswordEncoder.class));
        RegistrationForm form = new RegistrationForm();
        form.setEmail("participante@example.com");
        when(users.existsByEmail("participante@example.com")).thenReturn(true);

        var error = assertThrows(IllegalArgumentException.class, () -> service.register(form));
        assertEquals("Este email já está cadastrado.", error.getMessage());
        verify(users, never()).saveAndFlush(any());
    }
}
