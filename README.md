# EventIF — Sistema de Gestão de Eventos Acadêmicos

Trabalho final da disciplina Tópicos em Java para a Web (IFCE – Campus Fortaleza). Sistema web para divulgar eventos acadêmicos, receber inscrições em atividades, controlar presença e emitir certificados.

## Integrantes

- Marcos Iury
- Vinicius Andrade de Moura

## Vídeo

Link do vídeo explicativo: _(pendente — inserir o link do YouTube não listado ou do Google Drive antes da entrega)_

## Pré-requisitos

- Forma preferencial: Docker 24+ com o plugin Docker Compose (v2). Java, Maven e PostgreSQL não precisam estar instalados.
- Alternativa sem Docker: Java 21, Maven 3.9+ e PostgreSQL 16.

Tecnologias: Java 21, Spring Boot 3.5.16, Spring MVC + Thymeleaf, Spring Data JPA/Hibernate, Spring Security 6, PostgreSQL 16, Bootstrap 5 (WebJar local) e OpenHTMLtoPDF.

## Como subir com Docker (recomendado)

Na raiz do repositório:

```sh
cp .env.example .env
docker compose up --build
```

Acesse **http://localhost:8080/**. Para parar: `Ctrl+C` (ou `docker compose down`; acrescente `-v` para apagar também o banco).

O Compose sobe dois serviços: `db` (PostgreSQL 16, com healthcheck e volume `postgres_data`) e `app` (build multi-stage pelo `Dockerfile`). A aplicação só inicia depois que o banco está saudável. O Hibernate cria/atualiza as tabelas (`ddl-auto=update`) e o seed carrega os dados de exemplo na subida.

## Como subir sem Docker (Maven local)

1. Crie um banco PostgreSQL (por exemplo `eventif`) e um usuário com acesso a ele.
2. Exporte as variáveis de ambiente e execute:

```sh
export DB_URL=jdbc:postgresql://localhost:5432/eventif
export DB_USERNAME=seu_usuario
export DB_PASSWORD=sua_senha
export SEED_PASSWORD=test
export ADMIN_EMAIL=admin@test.com
export ORGANIZER_EMAIL=organizer@test.com
export PARTICIPANT_EMAIL=participant@test.com
export TZ=UTC
mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Duser.timezone=UTC
```

Também é possível usar só o banco do Compose (`docker compose up -d db`) e apontar `DB_URL` para `jdbc:postgresql://localhost:5432/<POSTGRES_DB do .env>`.

Testes automatizados (JUnit 5 + Mockito, não precisam de banco): `mvn test`. Sem Maven local: `docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-21 mvn -B test`.

## Variáveis de ambiente

Definidas no arquivo `.env` (copiado de `.env.example`, que contém apenas valores de desenvolvimento; o `.env` não é versionado).

| Variável | Uso | Exemplo |
|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Banco criado pelo Compose e credenciais usadas pela aplicação | `your_db_name` … |
| `POSTGRES_PORT` | Porta do PostgreSQL exposta em 127.0.0.1 | `5432` |
| `APP_PORT` | Porta externa da aplicação | `8080` |
| `APP_TIME_ZONE` | Fuso usado para comparar horários das atividades | `America/Fortaleza` |
| `SEED_PASSWORD` | Senha dos usuários de exemplo (gravada com BCrypt) | `test` |
| `ADMIN_EMAIL`, `ORGANIZER_EMAIL`, `PARTICIPANT_EMAIL` | Emails dos usuários de exemplo | ver abaixo |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexão JDBC (o Compose monta a partir das variáveis acima; defina manualmente só sem Docker) | — |

As credenciais do PostgreSQL valem na criação do volume; alterá-las depois exige `docker compose down -v`.

## Usuários de teste

Criados na subida da aplicação (seed idempotente), com os valores de `.env.example`:

| Perfil | Email | Senha |
|---|---|---|
| Administrador (`ROLE_ADMIN`) | `admin@test.com` | `test` |
| Organizador (`ROLE_ORGANIZADOR`) | `organizer@test.com` | `test` |
| Participante (`ROLE_PARTICIPANTE`) | `participant@test.com` | `test` |

O seed também cria 2 categorias (Tecnologia e Educação) e 2 eventos do organizador, cada um com uma atividade. Os eventos nascem em **rascunho** (regra RF05); para vê-los na página inicial, entre como organizador e clique em **Publicar** em "Meus eventos". Contas novas criadas em `/cadastro` recebem `ROLE_PARTICIPANTE`.

## Estrutura do projeto

```
src/main/java/br/edu/ifce/eventif
├── config/       SecurityFilterChain, BCrypt, Clock e inicialização do seed
├── controller/   Controllers MVC (rotas em português: /, /eventos, /cadastro, /login, /inscricoes, /certificados, /organizador, /admin)
├── dto/          Formulários com Bean Validation
├── entity/       6 entidades JPA (User, Role, Category, Event, Activity, Enrollment) e enums
├── exception/    Exceções de negócio
├── repository/   Interfaces Spring Data JPA (consultas derivadas e paginadas)
└── service/      Regras de negócio (RN01–RN07) e transações
src/main/resources
├── application.yml
└── templates/    Thymeleaf; layout único em fragments/layout.html
src/test/java     Testes JUnit 5/Mockito de RN01, RN02 e RN04
docs/             Requisitos, regras, decisões, checklist e relatório de validação
```

Mapeamento para o enunciado: Usuario=`User`, Perfil=`Role`, Categoria=`Category`, Evento=`Event`, Atividade=`Activity`, Inscricao=`Enrollment`; StatusEvento=`EventStatus` (DRAFT/PUBLISHED/CLOSED = RASCUNHO/PUBLICADO/ENCERRADO) e TipoAtividade=`ActivityType` (LECTURE/MINICOURSE/WORKSHOP = PALESTRA/MINICURSO/OFICINA). O código está em inglês por decisão da equipe; telas, mensagens e rotas estão em português.

## Extras implementados e pendências

- **EX03 — Certificado em PDF**: implementado com OpenHTMLtoPDF (`/certificados/{eventId}/pdf`), além da página imprimível.
- **EX04 — Testes automatizados**: implementado; 10 testes JUnit 5/Mockito cobrindo RN01, RN02 e RN04.
- **EX01 (lista de espera) e EX02 (conflito de horários)**: não implementados, por decisão de escopo.
- Limitação conhecida: a verificação de vagas (RN05) é feita antes de gravar a inscrição, sem lock; inscrições exatamente simultâneas podem ultrapassar a capacidade (risco aceito para o uso acadêmico).
- A carga horária da atividade é calculada a partir do início e do término e armazenada em minutos; é exibida em horas.

Relatório da auditoria final: [docs/validation-report.md](docs/validation-report.md).
