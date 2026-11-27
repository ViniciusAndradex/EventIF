package br.edu.ifce.eventif.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import br.edu.ifce.eventif.service.UserService;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserService users) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/eventos/**", "/cadastro", "/login", "/error", "/webjars/**").permitAll()
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/organizador", "/organizador/**").hasRole("ORGANIZADOR")
                        .requestMatchers("/inscricoes", "/inscricoes/**").hasRole("PARTICIPANTE")
                        .requestMatchers("/certificados", "/certificados/**").hasRole("PARTICIPANTE")
                        .anyRequest().authenticated())
                .formLogin(form -> 
                    form.loginPage("/login")
                    .usernameParameter("email")
                    .permitAll())
                .addFilterAfter(new CurrentRoleFilter(users), AnonymousAuthenticationFilter.class)
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .build();
    }
}
