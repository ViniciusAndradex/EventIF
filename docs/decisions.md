# Decisões finais

- [Contexto Mestre](project-context.md) é a fonte de autoridade; consultar antes de alterar comportamento. Não ampliar escopo.
- Java 21, Spring Boot 3.5.16/3.x, MVC server-side e Thymeleaf; Spring Data JPA/Hibernate, Spring Security 6, PostgreSQL, Maven, Dockerfile/Compose, Bootstrap 5 local, `application.yml` e `ddl-auto=update`. Sem SPA/React, Flyway, Lombok, MapStruct ou dependências desnecessárias.
- Arquitetura em camadas `controller → service → repository`, base `br.edu.ifce.eventif`, regras e transações em services, DTOs e Bean Validation. Código em inglês; rotas e interface em português. GET/POST/PUT/DELETE com `HiddenHttpMethodFilter`.
- Seis entidades, papel funcional único por usuário via ManyToMany, enums como String; sem entidade Certificate, contadores persistidos ou cascata destrutiva.
- Manter `/admin` e `/organizador`; não criar `/participante`. Identidade da sessão determina inscrições e certificados. Aplicar autorização e titularidade no servidor.
- Estado inicial DRAFT; publicação exige atividade; encerramento manual somente de PUBLISHED; sem reabertura. Revogação de ORGANIZADOR tem exceção explícita para encerrar DRAFT e bloqueia quando houver PUBLISHED.
- Inscrição e presença seguem as matrizes de status/tempo; concorrência de capacidade exige transação/lock. Usar Clock injetável quando necessário. Não implementar lista de espera nem conflito de horários.
- Catálogo público somente PUBLISHED, 9 por página, filtro opcional por categoria e ordem startDate/id crescente; detalhe de outros estados retorna 404.
- Certificado elegível em HTML imprimível e PDF OpenHTMLtoPDF, com nome, evento e carga somada das presenças.
- Testes automatizados somente RN01 em UserService, RN02 em EventService e ActivityService e RN04 em EnrollmentService, com JUnit 5/Mockito e sem PostgreSQL. As demais regras, inclusive concorrência, são verificadas e registradas manualmente.
- Entrega inclui repositório, README reprodutível, vídeo de até 10 minutos e tag final. Issue, commit, tag, release e vídeo exigem pedido explícito para serem criados.
