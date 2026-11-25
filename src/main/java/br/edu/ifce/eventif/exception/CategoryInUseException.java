package br.edu.ifce.eventif.exception;

public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException() {
        super("Não é possível excluir a categoria porque há eventos associados a ela.");
    }
}
