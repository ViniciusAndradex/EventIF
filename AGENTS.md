# Instruções para agentes

- Consulte `docs/` antes de modificar comportamento; se a documentação necessária não existir ou estiver incompleta, informe essa limitação.
- Use Java 21, Spring Boot 3.5.16, Spring MVC, Thymeleaf, Bootstrap 5, Maven, PostgreSQL e Docker Compose. O enunciado exige Spring Boot 3.x: não use 4.x. Configure o Hibernate com `update`.
- Use o pacote base `br.edu.ifce.eventif`. Escreva código em inglês e rotas e interface em português.
- Siga a arquitetura controller → service → repository; mantenha toda regra de negócio exclusivamente no service.
- Use GET, POST, PUT e DELETE, com `HiddenHttpMethodFilter` para os métodos enviados por formulários HTML.
- Modele seis entidades e roles exclusivas em relacionamento ManyToMany.
- Mantenha obrigatoriamente as rotas `/admin` e `/organizador`, conforme RS04; não crie `/participante`.
- Restrinja os testes automatizados às regras RN01, RN02 e RN04, usando JUnit 5 e Mockito.
- Use OpenHTMLtoPDF para certificados e disponibilize certificado imprimível.
- Não implemente lista de espera nem verificação de conflito de horários.
