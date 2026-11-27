package br.edu.ifce.eventif.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryForm {
    @NotBlank(message = "Informe o nome da categoria.")
    @Size(max = 255, message = "O nome deve ter até 255 caracteres.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
