# Roteiro do vídeo — EventIF (5 min)

## Antes de gravar

No `.env`, coloque `DEMO_DATA=true` e suba o projeto do zero:

```sh
docker compose down -v
docker compose up --build
```

Com `DEMO_DATA=true`, além do seed normal, a aplicação cria os exemplos abaixo. O log mostra os ids dos eventos (linhas `[DEMO]`). A cada subida, a **Oficina de Spring Boot** é reposicionada para estar acontecendo agora, então dá para reiniciar e regravar quantas vezes quiser. Depois da gravação, volte `DEMO_DATA=false`.

Senha de todos os usuários: `test` (valor de `SEED_PASSWORD`).

| Usuário | Perfil | Para que serve no vídeo |
|---|---|---|
| `participant@test.com` | Participante | Inscrições, "Minhas inscrições" e certificado |
| `organizer@test.com` | Organizador | Dono da "Semana de Tecnologia IFCE" e da "Jornada de Educação 2026" |
| `organizador2@test.com` | Organizador | Segundo organizador, para o teste do 403 |
| `admin@test.com` | Admin | Categorias e perfis |
| `ana.souza@test.com`, `bruno.lima@test.com`, `carla.mendes@test.com` | Participante | Inscritos já existentes (lista de presença e atividade lotada) |

| Evento | Dono | Situação | O que tem |
|---|---|---|---|
| Semana de Tecnologia IFCE | organizer | Publicado | **Oficina de Spring Boot** (acontecendo agora, 4 inscritos, presença liberada), **Palestra de Segurança Web** (1 vaga, já lotada pela Ana) e **Minicurso de JPA** (vagas livres, começa em 2 dias) |
| Jornada de Educação 2026 | organizer | Encerrado | Participante com presença em 2 atividades (5h30), então o certificado já existe |
| Congresso de Computação | organizador2 | Publicado | Evento do segundo organizador |
| Encontro de Tecnologia / Encontro de Educação | organizer | Rascunho | Seed padrão (podem ser publicados no vídeo) |

## 1. Abertura — 0:00 a 0:20 (Marcos)

Apresentar o EventIF, os dois integrantes e as tecnologias: Spring Boot, Thymeleaf, JPA, Spring Security, PostgreSQL e Docker.

## 2. Fluxos de cada perfil — 0:20 a 2:20

**Participante (Marcos, 0:20–1:00)** — entrar como `participant@test.com`:
- Na página inicial, abrir **Semana de Tecnologia IFCE** e mostrar as vagas restantes.
- Inscrever-se no **Minicurso de JPA**. Opcional: tentar a **Palestra de Segurança Web** → "Não há vagas disponíveis nesta atividade."
- Abrir "Minhas inscrições" e mostrar a coluna de presença.
- Abrir "Meus certificados" → **Jornada de Educação 2026** → página do certificado e botão "Abrir PDF".

**Organizador (Vinicius, 1:00–1:50)** — entrar como `organizer@test.com`:
- Em "Meus eventos", criar um evento (aparece como Rascunho), criar uma atividade e publicar.
- Abrir **Semana de Tecnologia IFCE** → Atividades → **Oficina de Spring Boot** → Inscritos. Marcar presença nos checkboxes e clicar em "Salvar presenças".
- Encerrar o evento criado no vídeo.

**Admin (Vinicius, 1:50–2:20)** — entrar como `admin@test.com`:
- Em "Categorias", criar uma categoria (ex.: "Saúde").
- Em "Usuários", conceder organizador ao **Bruno Lima** e depois retirar.

## 3. Segurança — 2:20 a 3:20 (Marcos)

- **Acesso negado por perfil:** logado como `participant@test.com`, digitar `/admin` na barra de endereço → 403. Digitar `/organizador/eventos` → 403.
- **Trocar o id na URL:** logado como `organizador2@test.com`, abrir `/organizador/eventos/{id}/editar` com o id da **Semana de Tecnologia IFCE** (aparece no log `[DEMO]`, ou passe o mouse no link "Ver evento" da página inicial) → 403.
- Explicar que o menu muda por perfil, mas que a proteção é feita no servidor.

## 4. Explicação do código — 3:20 a 4:50

**Entidades e relacionamentos (Vinicius, 3:20–3:50)** — pasta `entity/`. São 6 entidades:
- User ↔ Role: `@ManyToMany`
- Category → Event: 1 para N
- Event: N–1 com o organizador (User) e 1–N com Activity
- Activity → Enrollment: 1 para N
- Enrollment tem a restrição única `(user_id, activity_id)`

**Spring Security (Marcos, 3:50–4:20)** — `config/SecurityConfig.java`:
- as rotas públicas;
- `/organizador/**` só para ORGANIZADOR e `/admin/**` só para ADMIN;
- o login próprio por email (`UserService` como `UserDetailsService`) e o BCrypt.

**Regras de negócio (Vinicius, 4:20–4:50)** — ficam nos services, e o controller só chama o service. Exemplos:
- email único no `UserService`;
- vagas e inscrição duplicada no `EnrollmentService`;
- certificado só para evento encerrado no `CertificateService`;
- a checagem de dono que gera o 403 (`checkOwner` em `EventService`).

## 5. Encerramento — 4:50 a 5:00 (os dois)

Mencionar os extras: certificado em PDF e testes automatizados.
