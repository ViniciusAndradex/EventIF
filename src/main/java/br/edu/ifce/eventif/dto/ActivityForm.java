package br.edu.ifce.eventif.dto;

import br.edu.ifce.eventif.entity.ActivityType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class ActivityForm {
    @NotBlank(message = "Informe o título.") @Size(max = 255, message = "O título deve ter até 255 caracteres.")
    private String title;
    @NotNull(message = "Selecione o tipo.") private ActivityType type;
    @NotBlank(message = "Informe o instrutor.") @Size(max = 255, message = "O nome deve ter até 255 caracteres.")
    private String instructor;
    @NotNull(message = "Informe o início.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;
    @NotNull(message = "Informe o término.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;
    @NotNull(message = "Informe o número de vagas.") @Min(value = 1, message = "O número de vagas deve ser maior que zero.")
    private Integer capacity;
    public String getTitle() { return title; } public void setTitle(String v) { title = v; }
    public ActivityType getType() { return type; } public void setType(ActivityType v) { type = v; }
    public String getInstructor() { return instructor; } public void setInstructor(String v) { instructor = v; }
    public LocalDateTime getStartDateTime() { return startDateTime; } public void setStartDateTime(LocalDateTime v) { startDateTime = v; }
    public LocalDateTime getEndDateTime() { return endDateTime; } public void setEndDateTime(LocalDateTime v) { endDateTime = v; }
    public Integer getCapacity() { return capacity; } public void setCapacity(Integer v) { capacity = v; }
}
