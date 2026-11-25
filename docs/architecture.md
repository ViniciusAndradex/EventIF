# Arquitetura

O [Contexto Mestre](project-context.md) prevalece. Aplicação MVC renderizada no servidor, sem SPA/React. Java 21, Spring Boot 3.5.16 (linha 3.x obrigatória), Spring MVC, Thymeleaf, Spring Data JPA/Hibernate, Spring Security 6, PostgreSQL, Maven, Bootstrap 5 local, Dockerfile e Docker Compose. Configuração em `application.yml`, com `spring.jpa.hibernate.ddl-auto=update`. Sem Flyway, Lombok, MapStruct ou dependências sem necessidade.

## Camadas e pacotes

Pacote base `br.edu.ifce.eventif`; organizar sob ele controllers, services, repositories, entidades, enums, DTOs e configuração de segurança. Código em inglês; rotas, mensagens e interface em português.

`controller → service → repository`: controller recebe requisição, valida DTO de formulário com Bean Validation e prepara view/resposta; não acessa repository nem contém regra de negócio. Service aplica regras, autorização contextual e transações; repository persiste e consulta. Na inscrição, a checagem de capacidade e a criação são protegidas na mesma transação por lock apropriado. Injectar `Clock` quando a lógica temporal precisar dele. Valores de inscritos e vagas são calculados.

Services dependem das interfaces Spring Data em `repository`, como `UserRepository`, e o Spring cria suas implementações. A consulta de paginação de eventos utiliza `Page` e `Pageable`; o lock pessimista é configurado na interface de atividades.

Templates usam fragments, `th:object`/`th:field`, erros por campo e flash messages. OpenHTMLtoPDF gera o PDF do certificado; a mesma informação deve estar disponível em HTML imprimível.

## Rotas e métodos

| Área | Contrato |
|---|---|
| Públicas | `/`, `/eventos/**`, `/cadastro`, `/login`; catálogo e detalhe expõem apenas eventos publicados. |
| Administração | `/admin` e `/admin/**`, exclusivamente ADMIN. |
| Organização | `/organizador` e `/organizador/**`, exclusivamente ORGANIZADOR. |
| Demais | Autenticadas; inscrições e certificados usam a identidade da sessão. Não existe `/participante`. |

Usar recursos em plural e subrecursos quando fizer sentido. GET consulta/página, POST cria, PUT atualiza ou altera estado, DELETE remove. Habilitar `HiddenHttpMethodFilter` para PUT/DELETE em formulários. Os caminhos concretos além dos prefixos acima não foram definidos no contexto e devem preservar esse contrato. Uma rota pública não dispensa as verificações de identidade e papel das operações protegidas.

## Segurança

`SecurityFilterChain`, formulário, `UserDetailsService` por email e BCrypt. Cada usuário tem exatamente um papel funcional, sem hierarquia implícita entre ADMIN e ORGANIZADOR. Organização de evento/atividade exige titularidade, inclusive em URLs alteradas: retornar 403 para recurso de outro organizador. Inscrições e certificados são ligados ao usuário autenticado; `sec:authorize` apenas apresenta menus e não protege operações. Catálogo responde 404 para DRAFT/CLOSED. Ver regras de [status e tempo](business-rules.md).
