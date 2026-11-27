# EventIF — Sistema de Gestão de Eventos Acadêmicos

Trabalho final da disciplina **Tópicos em Java para a Web** (IFCE – Campus Fortaleza, 2026.2). O EventIF divulga eventos acadêmicos, recebe inscrições em atividades, controla a presença e emite certificados.

## Integrantes

| Nome completo | Matrícula | Usuário GitHub |
|---|---|---|
| Marcos Iury de Lima Américo | 20221045050281 | [MarcosAmericoDev](https://github.com/MarcosAmericoDev) |
| Vinicius Andrade de Moura | 20202045050498 | [ViniciusAndradex](https://github.com/ViniciusAndradex) |

## Vídeo

**Link:** _(preencher: YouTube não listado ou Google Drive com acesso por link)_

## Pré-requisitos

| Caminho | O que instalar |
|---|---|
| **Docker (recomendado)** | Docker 24+ com o plugin Docker Compose v2 (`docker compose version`). Não precisa de Java, Maven nem PostgreSQL. |
| Maven (alternativo) | Java 21 (JDK), Maven 3.9+ e PostgreSQL 16 em execução. |

Portas usadas por padrão: **8080** (aplicação) e **5432** (PostgreSQL, só em 127.0.0.1). Se estiverem ocupadas, troque `APP_PORT` e `POSTGRES_PORT` no `.env`.

Tecnologias: Java 21, Spring Boot 3.5.16, Spring MVC + Thymeleaf, Spring Data JPA/Hibernate, Spring Security 6, PostgreSQL 16, Bootstrap 5.3 (WebJar local) e OpenHTMLtoPDF.

## Como subir com Docker Compose (caminho preferencial)

Na raiz do repositório:

```sh
cp .env.example .env
docker compose up --build
```

Espere a linha `Started EventifApplication` no log e acesse **http://localhost:8080/**.

- O Compose sobe dois serviços: `db` (PostgreSQL 16 com healthcheck e volume `postgres_data`) e `app` (build multi-stage do `Dockerfile`, que também compila o projeto com Maven).
- A aplicação só inicia depois que o banco está saudável. O Hibernate cria as tabelas (`ddl-auto=update`) e o seed carrega os dados de demonstração.
- Para rodar em segundo plano: `docker compose up --build -d`. Para ver o log: `docker compose logs -f app`.
- Para parar: `Ctrl+C` ou `docker compose down`. Para apagar também o banco: `docker compose down -v`.

## Como subir sem Docker (alternativa Maven)

1. Com o PostgreSQL rodando, crie um banco e um usuário:

   ```sh
   psql -U postgres -c "CREATE USER eventif WITH PASSWORD 'eventif';"
   psql -U postgres -c "CREATE DATABASE eventif OWNER eventif;"
   ```

2. Na raiz do repositório, exporte as variáveis e inicie a aplicação:

   ```sh
   export DB_URL=jdbc:postgresql://localhost:5432/eventif
   export DB_USERNAME=eventif
   export DB_PASSWORD=eventif
   export SEED_PASSWORD=test
   export ADMIN_EMAIL=admin@test.com
   export ORGANIZER_EMAIL=organizer@test.com
   export PARTICIPANT_EMAIL=participant@test.com
   export APP_TIME_ZONE=America/Fortaleza
   export TZ=UTC
   mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Duser.timezone=UTC
   ```

3. Acesse **http://localhost:8080/**.

Também dá para usar só o banco do Compose: `cp .env.example .env`, `docker compose up -d db` e então `DB_URL=jdbc:postgresql://localhost:5432/<POSTGRES_DB>`, com `DB_USERNAME`/`DB_PASSWORD` iguais a `POSTGRES_USER`/`POSTGRES_PASSWORD` do `.env`.

## Variáveis de ambiente

No Docker, ficam no arquivo `.env`, copiado de `.env.example`. O `.env.example` só tem valores de demonstração, e o `.env` não é versionado.

| Variável | Obrigatória | Uso | Valor do exemplo |
|---|---|---|---|
| `POSTGRES_DB` | sim (Docker) | Nome do banco criado pelo Compose | `your_db_name` |
| `POSTGRES_USER` | sim (Docker) | Usuário do banco | `your_db_username` |
| `POSTGRES_PASSWORD` | sim (Docker) | Senha do banco | `your_db_password` |
| `POSTGRES_PORT` | não | Porta do PostgreSQL no host (só 127.0.0.1) | `5432` |
| `APP_PORT` | não | Porta da aplicação no host | `8080` |
| `APP_TIME_ZONE` | não | Fuso usado para comparar os horários das atividades | `America/Fortaleza` |
| `SEED_PASSWORD` | sim | Senha dos usuários de demonstração (gravada com BCrypt) | `test` |
| `ADMIN_EMAIL`, `ORGANIZER_EMAIL`, `PARTICIPANT_EMAIL` | sim | Emails dos usuários de demonstração | ver abaixo |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | sim (Maven) | Conexão JDBC. No Docker, o Compose monta a partir das variáveis `POSTGRES_*`. | — |

As credenciais do PostgreSQL valem na criação do volume. Para trocá-las depois, rode `docker compose down -v` antes de subir de novo.

## Credenciais de demonstração

O seed cria os usuários na subida da aplicação e não duplica nada se ela for reiniciada. Com os valores do `.env.example`:

| Perfil | Email | Senha |
|---|---|---|
| Administrador (`ROLE_ADMIN`) | `admin@test.com` | `test` |
| Organizador (`ROLE_ORGANIZADOR`) | `organizer@test.com` | `test` |
| Participante (`ROLE_PARTICIPANTE`) | `participant@test.com` | `test` |

O seed também cria 2 categorias (Tecnologia e Educação) e 2 eventos do organizador, cada um com uma atividade. **Os eventos nascem em rascunho** (RF05). Para vê-los na página inicial, entre como organizador e clique em **Publicar** em "Meus eventos". Toda conta criada em `/cadastro` recebe `ROLE_PARTICIPANTE`.

Esses valores servem só para demonstração; não os use em produção.

## Funcionalidades

| Perfil | O que faz | Rotas |
|---|---|---|
| Visitante | Vê os eventos publicados (paginados, com filtro por categoria), os detalhes do evento com as vagas restantes e cria conta | `/`, `/eventos/{id}`, `/cadastro`, `/login` |
| Participante | Inscreve-se em atividades, cancela antes do início, vê "Minhas inscrições" com a presença e emite o certificado (HTML imprimível e PDF) de eventos encerrados | `/inscricoes`, `/certificados` |
| Organizador | CRUD dos próprios eventos (nascem em rascunho), publica e encerra, CRUD de atividades, lista os inscritos e marca presença por checkboxes | `/organizador/**` |
| Administrador | CRUD de categorias, lista usuários e concede ou retira o perfil de organizador | `/admin/**` |

Regras de negócio (RN01–RN07) ficam nos services, e cada violação mostra uma mensagem na tela: email único, datas válidas, inscrição só em evento publicado, sem inscrição duplicada, sem inscrição com vagas esgotadas, cancelamento só antes do início e certificado só para evento encerrado com presença.

Segurança: login por formulário próprio, senhas com BCrypt, regras por URL no `SecurityFilterChain`, CSRF em todos os formulários e verificação do dono no service. Um organizador que troca o id na URL para o evento de outro recebe **403**.

## Estrutura do projeto

```
src/main/java/br/edu/ifce/eventif
├── EventifApplication.java
├── config/       SecurityConfig (SecurityFilterChain), CurrentRoleFilter, DataInitializer (BCrypt, Clock, seed)
├── controller/   Controllers MVC: Home, Auth, Enrollment, Certificate, Organizer*, Category, Admin
├── dto/          Formulários com Bean Validation (@NotBlank, @Email, @Positive…)
├── entity/       6 entidades JPA + enums EventStatus e ActivityType
├── exception/    Exceções de negócio
├── repository/   Spring Data JPA (consultas derivadas e paginada)
└── service/      Regras de negócio e transações (RN01–RN07), UserDetailsService e seed
src/main/resources
├── application.yml
└── templates/    Thymeleaf; layout único em fragments/layout.html (cabecalho, menu, rodape)
src/test/java     Testes JUnit 5 + Mockito (RN01, RN02, RN04)
docs/             Requisitos, regras, decisões, checklist e relatório de validação
Dockerfile, docker-compose.yml, .env.example
```

Mapeamento dos nomes do enunciado para o código, que está em inglês por decisão da equipe (telas, mensagens e rotas estão em português):

| Enunciado | Código |
|---|---|
| Usuario, Perfil, Categoria | `User`, `Role`, `Category` |
| Evento, Atividade, Inscricao | `Event`, `Activity`, `Enrollment` |
| StatusEvento RASCUNHO / PUBLICADO / ENCERRADO | `EventStatus` DRAFT / PUBLISHED / CLOSED |
| TipoAtividade PALESTRA / MINICURSO / OFICINA | `ActivityType` LECTURE / MINICOURSE / WORKSHOP |
| cargaHoraria (h) | `workloadMinutes`, calculada pela duração e exibida em horas |

## Extras implementados

1. **Certificado em PDF:** gerado com OpenHTMLtoPDF em `/certificados/{eventId}/pdf`, além da página imprimível.
2. **Testes automatizados:** 10 testes JUnit 5/Mockito, sem banco, das regras RN01 (`UserService`), RN02 (`EventService`, `ActivityService`) e RN04 (`EnrollmentService`).

Relatório de validação e segurança: [docs/validation-report.md](docs/validation-report.md).

## Comandos finais de validação

```sh
# 1. Testes automatizados (precisa de Java 21 + Maven)
mvn test

# 1b. Mesmo teste sem Maven local, via Docker
#     (remove o target/ no fim, porque dentro do contêiner ele é criado como root)
docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-21 sh -c 'mvn -B test; s=$?; rm -rf target; exit $s'

# 2. Subida do zero, exatamente como acima
docker compose down -v
cp .env.example .env
docker compose up --build -d
docker compose logs -f app        # aguardar "Started EventifApplication"; Ctrl+C sai do log

# 3. Verificações rápidas
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/            # 200
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/admin       # 302 (redireciona para o login)

# 4. Repositório sem segredos nem artefatos gerados
git status --short                 # nada inesperado
git ls-files | grep -E '^\.env$|target/|\.idea/' || echo "ok"
```
