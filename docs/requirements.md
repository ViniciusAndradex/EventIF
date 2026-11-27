# Requisitos

Fonte normativa: [Contexto Mestre](project-context.md). Os identificadores RF, RS, RI e RT abaixo são uma organização documental dos itens fornecidos: o contexto não associa explicitamente cada número a uma descrição. RN01–RN07 e RS04 seguem as referências expressas no contexto. Não se presume acesso ao enunciado original.

## Funcionais

| ID | Requisito |
|---|---|
| RF01 | Cadastro público cria usuário PARTICIPANTE. |
| RF02 | Autenticação por email e senha, login e logout. |
| RF03 | ADMIN cria, consulta, altera e remove categorias. |
| RF04 | ADMIN lista usuários e concede ou retira ORGANIZADOR, respeitando as regras de papel e eventos. |
| RF05 | ORGANIZADOR gerencia exclusivamente seus eventos. |
| RF06 | ORGANIZADOR gerencia atividades de seus eventos. |
| RF07 | ORGANIZADOR publica e encerra seus eventos conforme as transições permitidas. |
| RF08 | Catálogo público lista eventos PUBLISHED, 9 por página, com filtro opcional por categoria e ordenação por startDate e id ascendentes. |
| RF09 | Detalhe público de evento PUBLISHED apresenta atividades e vagas restantes; DRAFT e CLOSED retornam 404 no catálogo. |
| RF10 | PARTICIPANTE inscreve-se em atividade elegível e cancela sua inscrição nas condições permitidas. |
| RF11 | PARTICIPANTE consulta suas inscrições e presenças usando a identidade da sessão. |
| RF12 | ORGANIZADOR lista inscritos e registra presença em suas atividades conforme tempo e status. |
| RF13 | Usuário elegível acessa certificado próprio em HTML imprimível e PDF com nome, evento e soma da carga das atividades com presença. |
| RF14 | Seed idempotente cria duas categorias, dois eventos com atividades e um usuário de cada perfil, incluindo ADMIN. |

## Regras de negócio

| ID | Regra |
|---|---|
| RN01 | Email é único. |
| RN02 | Fim do evento não antecede início; fim da atividade é posterior ao início e ambos estão no período inclusivo do evento. |
| RN03 | Inscrição somente em evento PUBLISHED, sujeita também às regras temporais. |
| RN04 | Um usuário não pode ter inscrição duplicada na mesma atividade. |
| RN05 | Verificar a capacidade antes da inscrição; não há garantia contra inscrições simultâneas. |
| RN06 | Cancelamento somente antes do início da atividade e enquanto o evento não estiver CLOSED. |
| RN07 | Certificado somente para evento CLOSED e usuário com ao menos uma presença nele. |

As regras adicionais de ciclo de vida, revogação, carga horária e tempo constam em [business-rules.md](business-rules.md).

## Segurança

| ID | Requisito |
|---|---|
| RS01 | Autenticação por formulário, UserDetailsService por email e senhas BCrypt. |
| RS02 | `/`, `/eventos/**`, `/cadastro` e `/login` são públicos; demais rotas exigem autenticação, observadas as restrições seguintes. |
| RS03 | `/organizador/**` exige exclusivamente ORGANIZADOR; acesso a evento ou atividade de outro organizador retorna 403. |
| RS04 | Manter `/admin` e `/organizador`; não criar `/participante`. |
| RS05 | `/admin/**` exige exclusivamente ADMIN; papéis funcionais são exclusivos. |
| RS06 | Inscrições e certificados usam usuário da sessão; não confiar em identificador de usuário fornecido pelo cliente. |
| RS07 | `sec:authorize` controla apresentação de menus, nunca substitui autorização no servidor. |

## Interface

| ID | Requisito |
|---|---|
| RI01 | Renderização no servidor com Thymeleaf e Bootstrap 5 local; layout em fragments. |
| RI02 | Formulários com `th:object`, `th:field`, erro por campo e DTOs com Bean Validation. |
| RI03 | Telas, mensagens e rotas em português; usar flash messages nas operações aplicáveis. |

## Técnicos e extras

| ID | Requisito |
|---|---|
| RT01 | Java 21, Spring Boot 3.5.16 (3.x, não 4.x), MVC, Security 6, Data JPA/Hibernate, Maven e PostgreSQL. |
| RT02 | Pacote `br.edu.ifce.eventif`, camadas controller → service → repository, código em inglês; regras e transações em services. |
| RT03 | Git, Dockerfile, Docker Compose, `application.yml`, Hibernate `ddl-auto=update`; sem Flyway, Lombok, MapStruct ou dependências desnecessárias. |
| RT04 | GET consulta/página, POST cria, PUT atualiza/ação de estado, DELETE remove; habilitar `HiddenHttpMethodFilter`. |
| RT05 | Exatamente seis entidades JPA, enums como String, unicidade de email e de inscrição, verificação de capacidade e nenhuma cascata destrutiva. |
| EX03 | Certificado PDF com OpenHTMLtoPDF, além de página HTML imprimível. |
| EX04 | JUnit 5 e Mockito, sem PostgreSQL; automatizar somente RN01, RN02 e RN04 nos services indicados no contexto. |
