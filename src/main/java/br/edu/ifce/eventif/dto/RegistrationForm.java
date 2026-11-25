package br.edu.ifce.eventif.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {
    @NotBlank(message = "Informe seu nome.") @Size(max = 255)
    private String name;
    @NotBlank(message = "Informe seu email.") @Email(message = "Informe um email válido.") @Size(max = 255)
    private String email;
    @NotBlank(message = "Informe sua senha.") @Size(max = 72, message = "A senha deve ter até 72 caracteres.")
    private String password;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
