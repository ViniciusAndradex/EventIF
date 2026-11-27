# Relatório de validação final — EventIF

- **Data:** 26/11/2026 (horário local America/Fortaleza; 27/11 em UTC)
- **Branch:** `feat/auditoria-final` (base `main` em `6c44249`)
- **Fonte normativa:** enunciado do professor `TJW___2026_2___TF.pdf` (prevalece) e, em seguida, [requirements.md](requirements.md). **A numeração usada aqui é a do enunciado.**
- **Ambiente:** Java 21 / Maven 3.9 no contêiner `maven:3.9-eclipse-temurin-21`; aplicação + PostgreSQL 16 via Docker Compose. A validação manual rodou numa pilha isolada (`docker compose -p eventif-audit`, portas 8081/5433, volume próprio, removido no fim), com requisições HTTP reais (sessão, CSRF, `_method`) e conferência por SQL no banco.

## 1. Build e testes

| Comando | Resultado |
|---|---|
| `mvn -B clean test` (antes das correções) | BUILD SUCCESS — 10 testes, 0 falhas, 0 erros |
| `mvn -B clean test` (depois das correções) | BUILD SUCCESS — 10 testes, 0 falhas, 0 erros |
| `docker compose up --build` (`mvn package` no Dockerfile) | Imagem gerada; aplicação iniciou em ~6 s; `GET /` → 200 |

Testes: `UserServiceTest` (RN01, 1), `EventServiceTest` (RN02, 3), `ActivityServiceTest` (RN02, 5), `EnrollmentServiceTest` (RN04, 1). Nenhum teste novo foi criado.

## 2. Lacunas encontradas e corrigidas

| # | Item do enunciado | Lacuna | Correção |
|---|---|---|---|
| 1 | Modelo de domínio: "Categoria @OneToMany com Evento" | `Category` não mapeava o lado `@OneToMany`; só existia `Event @ManyToOne Category`. | `Category.events` com `@OneToMany(mappedBy = "category")`, sem cascade, getter somente leitura. Não altera o schema. |
| 2 | RF08 "marcar presença (checkboxes)" | A tela de inscritos tinha um botão "Marcar presença" por linha e não permitia desmarcar. | Tela com um checkbox por inscrito e botão "Salvar presenças" (`PUT .../inscricoes/presenca`). `EnrollmentService.updatePresence` mantém as mesmas regras (dono do evento → 403; evento CLOSED e fora do horário da atividade → bloqueado; inscrição de outra atividade → 404). |
| 3 | RI01 "layout único com fragments (cabeçalho, menu e rodapé)" | Havia um único fragment `layout`; cabeçalho, menu e rodapé não eram fragments nomeados. Admin não tinha link de Categorias no menu. | `fragments/layout.html` com `th:fragment="cabecalho"`, `"menu"` e `"rodape"`; menu do ADMIN ganhou "Usuários" e "Categorias"; menu do organizador passou a se chamar "Meus eventos". |
| 4 | RT03 cita `@Positive` | `ActivityForm.capacity` usava `@Min(1)`. | Trocado por `@Positive` (mesma mensagem). |
| 5 | RT05 / Seção 10 (README) | O README dizia que autenticação e fluxos "ainda não foram implementados", citava emails de seed que não existem e não tinha integrantes, vídeo, usuários de teste, extras nem pendências. | README reescrito com todos os itens mínimos da Seção 10. **O link do vídeo ficou como pendente** e precisa ser preenchido pela equipe. |

## 3. Requisitos funcionais (RF01–RF14)

| ID | Enunciado | Resultado | Evidência |
|---|---|---|---|
| RF01 | Cadastro público; conta nova recebe ROLE_PARTICIPANTE | ✅ | `POST /cadastro` → 302 `/login`; banco: papel `ROLE_PARTICIPANTE`, `active=t`, hash `$2a$`. |
| RF02 | Login e logout por formulário próprio | ✅ | Login correto → 302 `/`; senha errada → `/login?error`; logout → `/login?logout`; depois do logout, `/inscricoes` → 302 `/login`. |
| RF03 | (Admin) CRUD de Categoria | ✅ | Criou, editou e excluiu categoria, todas com mensagem flash; nome vazio re-renderiza com erro de campo; excluir categoria em uso → "Não é possível excluir a categoria porque há eventos associados a ela." (registro mantido). |
| RF04 | (Admin) listar usuários, conceder/retirar organizador | ✅ | Conceder → `ROLE_ORGANIZADOR`; retirar `ROLE_ORGANIZADOR` → `ROLE_PARTICIPANTE` se os eventos tiverem em rascunho ou encerrado; retirar com evento publicado → bloqueado; alterar ADMIN → bloqueado; a sessão antiga é invalidada (302 `/login`). |
| RF05 | CRUD de eventos próprios; nasce em RASCUNHO | ✅ | Criado com `status=DRAFT`; editar e excluir DRAFT sem atividades OK; erros de campo por campo. |
| RF06 | Publicar e encerrar | ✅ | Publicar sem atividade → bloqueado; com atividade → PUBLISHED; encerrar → CLOSED; encerrar de novo, publicar CLOSED e editar CLOSED → bloqueados com mensagem. |
| RF07 | CRUD de atividades do evento | ✅ | Criar e editar (tipo, horários, vagas; carga recalculada de 120 para 90 min); excluir a última atividade de evento publicado → bloqueado. |
| RF08 | Listar inscritos e marcar presença (checkboxes) | ✅ (corrigido) | Página com `<input type="checkbox" name="presentes">`; marcar A → A=t, B=f; marcar B → A=f, B=t; os dois → t/t; id de outra atividade → 404; antes do início → "A presença só pode ser marcada durante a atividade."; em CLOSED → "Eventos encerrados não permitem alterações de presença." |
| RF09 | Página inicial pública, PUBLICADOS, paginada, filtro por categoria | ✅ | 11 publicados: página 0 com 9 cards, página 1 com 2; filtro da categoria 2 mostra só os seus 5; DRAFT/CLOSED não aparecem. Consulta paginada `findByStatus…(…, Pageable)`. |
| RF10 | Detalhes do evento com atividades e vagas restantes | ✅ | `/eventos/3` mostra "Vagas restantes"; vai de 1 para 0 depois da inscrição. DRAFT/CLOSED/inexistente → 404. |
| RF11 | Inscrever-se em atividade | ✅ | "Inscrição realizada."; organizador e admin → 403. |
| RF12 | Cancelar a própria inscrição | ✅ | Antes do início → "Inscrição cancelada." e registro removido. |
| RF13 | "Minhas inscrições" com presença | ✅ | Lista evento, atividade, início e "Presente"/"Não registrada" por atividade. |
| RF14 | Certificado imprimível (evento ENCERRADO; nome, evento, carga com presença) | ✅ | `/certificados/3` → 200 com "Participante", "Semana" e "503 horas e 59 minutos" (= soma SQL de 30 239 min das atividades com presença); botão Imprimir e `@media print`; PDF 200 `application/pdf`. |

## 4. Regras de negócio (RN01–RN07)

Todas ficam nos services (`UserService`, `EventService`, `ActivityService`, `EnrollmentService`, `CertificateService`) e mostram mensagem ao usuário.

| ID | Resultado | Evidência |
|---|---|---|
| RN01 Email único | ✅ automatizado + manual | `UserServiceTest`; cadastro repetido → erro no campo email: "Este email já está cadastrado." (1 registro); constraint `uk_users_email UNIQUE (email)`. |
| RN02 Datas | ✅ automatizado + manual | Testes de `EventService`/`ActivityService`; evento com fim < início → bloqueado; atividade com fim < início ou fim = início → "O término deve ser posterior…"; antes do evento ou depois do último dia → "A atividade deve estar dentro das datas do evento."; limites inclusivos (00:00 do 1º dia a 23:59 do último) aceitos. |
| RN03 Só evento PUBLICADO | ✅ manual | Inscrição em DRAFT → "Inscrições são permitidas apenas em eventos publicados." |
| RN04 Sem inscrição duplicada | ✅ automatizado + manual | `EnrollmentServiceTest`; segunda inscrição → "Você já está inscrito nesta atividade."; constraint `uk_enrollment_user_activity UNIQUE (user_id, activity_id)`. |
| RN05 Sem inscrição com vagas esgotadas | ✅ manual (sequencial) | Atividade com 1 vaga: 1ª inscrição aceita, a de outro participante → "Não há vagas disponíveis nesta atividade."; banco com 1 inscrito. **A falta de lock para inscrições simultâneas é decisão registrada da equipe e foi mantida** (sem `@Lock`). |
| RN06 Cancelar só antes do início | ✅ manual | Durante a atividade → "O cancelamento só é permitido antes do início da atividade."; antes → cancelado; em CLOSED → bloqueado. |
| RN07 Certificado só para ENCERRADO com presença | ✅ manual | Antes de encerrar → 404 e lista vazia; depois de encerrar, participantes com presença → 200; inscrito sem presença → 404 na página e no PDF; evento PUBLISHED → 404. |

## 5. Segurança (RS01–RS07)

| ID | Resultado | Evidência |
|---|---|---|
| RS01 SecurityFilterChain + login próprio | ✅ | `SecurityConfig.securityFilterChain`, `formLogin().loginPage("/login")` com `login.html` próprio. |
| RS02 UserDetailsService por email | ✅ | `UserService implements UserDetailsService`, `findByEmail`; `usernameParameter("email")`. |
| RS03 BCrypt | ✅ | Bean `BCryptPasswordEncoder` em `DataInitializer`; hashes `$2a$` no banco. |
| RS04 Regras por URL | ✅ | Anônimo: `/`, `/eventos/**`, `/cadastro`, `/login` acessíveis; `/inscricoes`, `/certificados`, `/organizador/**`, `/admin/**` → 302 `/login`. `/admin/**`: só ADMIN (200; demais 403). `/organizador/**`: só ORGANIZADOR. O restante exige login (`anyRequest().authenticated()`). |
| RS05 Organizador só altera os próprios eventos (403) | ✅ | Segundo organizador no evento alheio: GET editar, atividades, nova, editar atividade, inscritos; PUT editar, encerrar, presença; DELETE evento e atividade → **todos 403**; nada mudou no banco; com navegador, aparece a página "Acesso negado". |
| RS06 Participante só vê/cancela as próprias inscrições | ✅ | Controllers usam `Principal`, nunca um id de usuário vindo do formulário; cancelar a inscrição de outro → 403 (registro mantido); "Minhas inscrições" de outro usuário não mostra a inscrição alheia. |
| RS07 Menus com `sec:authorize` + ADMIN criado na subida | ✅ | Menus: anônimo (Entrar/Cadastro), admin (Usuários/Categorias), organizador (Meus eventos), participante (Minhas inscrições/Meus certificados). `admin@test.com` criado pelo seed. |

## 6. Interface (RI01–RI03) e telas

| ID | Resultado | Evidência |
|---|---|---|
| RI01 Layout único com fragments | ✅ (corrigido) | Todas as páginas usam `th:replace="~{fragments/layout :: layout(…)}"` (exceto o certificado, que é uma página própria para impressão/PDF); fragments `cabecalho`, `menu` e `rodape`. |
| RI02 `th:object`/`th:field` + erro por campo | ✅ | Formulários de cadastro, categoria, evento e atividade; erros por campo conferidos em cadastro, evento e atividade. |
| RI03 Flash após cada ação | ✅ | `RedirectAttributes.addFlashAttribute` em todas as ações POST/PUT/DELETE; mensagens de sucesso e erro conferidas. |

Telas mínimas (10/10): 1 `index.html`; 2 `eventos/detalhe.html`; 3 `login.html` + `cadastro.html`; 4 `inscricoes/lista.html`; 5 `certificados/certificado.html` (+ `certificados/lista.html`); 6 `organizador/eventos/lista.html`; 7 `organizador/eventos/formulario.html`; 8 `organizador/atividades/lista.html` + `formulario.html`; 9 `organizador/atividades/inscritos.html`; 10 `admin/categorias/*` + `admin/usuarios.html`. Também há páginas de erro 403/404/500.

## 7. Requisitos técnicos (RT01–RT05)

| ID | Resultado | Evidência |
|---|---|---|
| RT01 Camadas sem regra no controller | ✅ | Nenhum controller importa repository; controllers só fazem binding, chamam o service e montam o flash. |
| RT02 Consulta derivada + paginada | ✅ | Derivadas: `findByEmail`, `existsByUserIdAndActivityId`, `countByActivityId`, `findByOrganizerIdAndStatus`, etc. Paginada: `findByStatusOrderByStartDateAscIdAsc(EventStatus, Pageable)` e a variante por categoria, 9 por página. |
| RT03 Bean Validation | ✅ | `@NotBlank`, `@Email`, `@Positive`, `@NotNull`, `@Size` nos DTOs com `@Valid` nos controllers. |
| RT04 Seed | ✅ | Banco novo: 2 categorias (Tecnologia, Educação), 2 eventos com 1 atividade cada, 1 usuário por perfil. Seed idempotente (busca antes de criar). |
| RT05 README na raiz | ✅ (corrigido) | Todos os itens da Seção 10; link do vídeo pendente. |

## 8. Verificações estruturais

- **Exatamente 6 entidades JPA:** `User`, `Role`, `Category`, `Event`, `Activity`, `Enrollment` (6 `@Entity`); certificado não é entidade. Tabelas: as 6 + a tabela de junção `user_roles`.
- **Relacionamentos obrigatórios:** User↔Role `@ManyToMany`; Category `@OneToMany` Event (corrigido); Event `@ManyToOne` Category e User (organizador), `@OneToMany` Activity; Activity `@ManyToOne` Event, `@OneToMany` Enrollment; Enrollment `@ManyToOne` User e Activity com unicidade `(user_id, activity_id)`.
- **Enums** com `@Enumerated(EnumType.STRING)` (coluna `status` é `character varying`). Datas com `java.time`.
- Sem `cascade`, `orphanRemoval` ou `@Lock`.

## 9. Pontos de atenção (não corrigidos, decisões registradas)

1. **Nomes em inglês.** O enunciado usa nomes em português (Usuario, Perfil, StatusEvento RASCUNHO/PUBLICADO/ENCERRADO, TipoAtividade PALESTRA/MINICURSO/OFICINA, `cargaHoraria`). O código usa os equivalentes em inglês por decisão da equipe; a tela mostra os rótulos em português. Os nomes de role (`ROLE_PARTICIPANTE`, `ROLE_ORGANIZADOR`, `ROLE_ADMIN`) são os do enunciado. O mapeamento está no README. Renomear os enums mudaria os valores já gravados no banco, então não foi feito. Vale explicar isso na arguição.
2. **Carga horária** é calculada pela duração da atividade e gravada em minutos (`workloadMinutes`); a tela e o certificado mostram horas.
3. **RN05 sem lock**: inscrições exatamente simultâneas podem ultrapassar as vagas (risco aceito, registrado em `decision-log.md`).
4. **Seed em rascunho**: os 2 eventos do seed nascem DRAFT (coerente com RF05), então a página inicial começa vazia até o organizador publicar. O README explica como publicar.
5. **Numeração de `requirements.md`** não bate com a do enunciado (ex.: lá RF08 é o catálogo e RS04 é "manter /admin"). O conteúdo está coberto; este relatório usa a numeração do enunciado.
6. **Pendências de entrega** (fora do código): link do vídeo no README, tag `final`, repositório acessível ao professor.
7. Extras EX01/EX02 não foram implementados, de propósito.

## 10. Revisão de segurança (inspeção + execução manual)

Pilha isolada (`eventif-audit`, removida no fim) com 2 organizadores (organizer, bob), 2 participantes (participant, ana), eventos e atividades de cada dono e um evento CLOSED com presença. As requisições foram feitas com sessões reais.

### 10.1 Resultados

| Item | Resultado | Evidência resumida |
|---|---|---|
| CSRF | ✅ | CSRF ativo (não há `csrf().disable()`). Os 16 formulários POST usam `th:action` e recebem `_csrf` (conferido em todas as telas). Sem token ou com token falso → **403** e nada muda no banco: login, cadastro, logout, inscrição, cancelamento, categoria, conceder organizador, encerrar evento e presença. |
| BCrypt | ✅ | Todos os usuários (seed e cadastro) com hash `$2a$10$`, 60 caracteres. |
| Login próprio | ✅ | `/login` renderiza `login.html` ("Entrar"), não a página padrão do Spring; o id da sessão muda no login (proteção contra fixação de sessão). |
| UserDetailsService por email | ✅ | Login com o nome ou com o parâmetro `username` → `/login?error`; só `email` + senha autentica. |
| RS04 regras por URL | ✅ | Matriz anônimo/participante/organizador/admin: públicas `/`, `/eventos/**`, `/cadastro`, `/login` → 200 para todos. `/inscricoes` e `/certificados`: só participante. `/organizador/**`: só organizador. `/admin/**`: só admin (demais 403; anônimo → login). Outras rotas exigem login (anônimo → 302 `/login`; logado → 404). |
| `/admin` e `/organizador` | ✅ mantidos | São os prefixos exigidos pelo enunciado (RS04) e continuam protegidos por `hasRole` no `SecurityFilterChain`. |
| `/participante` | ✅ não existe | Nenhuma rota no código; logado → 404. |
| RS07 menu só complementa | ✅ | O participante não vê os links de admin, mas o acesso direto `GET /admin/categorias/nova`, `POST /admin/categorias` e `PUT /admin/usuarios/1/organizador` → **403**. |
| RS05 dono do evento | ✅ (corrigido) | bob no evento/atividade do organizer: 5 GETs e 8 operações de escrita (editar, publicar, encerrar, excluir evento; criar, editar, excluir atividade; presença) → **403**; banco inalterado. Trocar só o id da atividade (evento próprio + atividade alheia), nos dois sentidos → **403**. |
| Troca de id de inscrição (presença) | ✅ (corrigido) | Na própria atividade, enviar `presentes` com o id de uma inscrição de outro organizador → **403** (sem alteração); id inexistente → 404; id da própria lista → salvo. |
| RS06 sessão | ✅ | ana cancelando a inscrição do participant → **403** (registro mantido); `userId`/`user.id` forjados no POST são ignorados (a inscrição fica para ana); "Minhas inscrições" só lista as do usuário logado; organizador/admin em `/inscricoes` → 403. |
| Certificado de outro usuário | ✅ | A URL tem só o `eventId`; o usuário sempre vem da sessão. ana (sem presença) em `/certificados/30` e `/pdf` → 404, inclusive com `?userId=1&email=…`, e o nome do outro participante nunca aparece. Dono da presença → 200. Organizador/admin → 403; anônimo → login. |
| XSS | ✅ | Nenhum `th:utext` nem inline sem escape; tudo sai com `th:text` (escapado). |
| Erros | ✅ | Sem stacktrace nas respostas; páginas 403/404/500 próprias no navegador. |
| Segredos | ✅ | Só `.env.example` (valores de desenvolvimento) é versionado; `.env` está no `.gitignore`; PostgreSQL exposto só em 127.0.0.1. |

### 10.2 Vulnerabilidades encontradas e corrigidas

| # | Problema | Correção | Reteste |
|---|---|---|---|
| S1 | **Erro 500 em rota pública:** `GET /?pagina=2147483647` → `InvalidDataAccessApiUsageException: Page offset exceeds Integer.MAX_VALUE`. Qualquer visitante anônimo gerava erro interno e stacktrace no log. | `CatalogService.list` limita a página a `Integer.MAX_VALUE / 9`; páginas além do fim voltam vazias. | 200 (página vazia); a paginação normal continua igual. |
| S2 | **Troca de id respondia 404 em vez de 403 (RS05):** em `/organizador/eventos/{meuEvento}/atividades/{atividadeAlheia}/inscricoes` (GET e PUT de presença) e com `presentes` contendo inscrição de outro organizador. Nenhum dado vazava, mas a resposta era diferente do `ActivityService` e do pedido do enunciado ("trocar o id na URL deve resultar em 403"). | `EnrollmentService.activityForEvent` confere primeiro o dono do evento da atividade (403) e depois se ela pertence ao evento da URL (404). `updatePresence` responde 403 para inscrições de outro organizador. | Todos os casos → 403; atividade própria em outro evento próprio → 404; o dono continua 200. |

Depois das correções: `mvn clean test` → 10 testes, 0 falhas; app principal recriado.

### 10.3 Observações (sem mudança)

- O cadastro não exige tamanho mínimo de senha (só `@NotBlank` e no máximo 72). O enunciado não pede política de senha; fica como melhoria possível.
- Certificado de quem não é elegível responde 404 (e não 403): não existe "certificado de outro usuário" endereçável, porque o recurso é sempre o do usuário da sessão.
- O cadastro informa "Este email já está cadastrado.", o que revela se um email existe; é consequência direta do RN01 com mensagem clara.

## 11. Arquivos alterados nesta auditoria

- `src/main/java/br/edu/ifce/eventif/entity/Category.java`
- `src/main/java/br/edu/ifce/eventif/dto/ActivityForm.java`
- `src/main/java/br/edu/ifce/eventif/service/EnrollmentService.java` (checkboxes de presença; dono antes do 404 na revisão de segurança)
- `src/main/java/br/edu/ifce/eventif/service/CatalogService.java` (limite de página)
- `src/main/java/br/edu/ifce/eventif/repository/EnrollmentRepository.java` (listas em ordem fixa)
- `src/main/java/br/edu/ifce/eventif/controller/OrganizerEnrollmentController.java`
- `src/main/resources/templates/fragments/layout.html`
- `src/main/resources/templates/organizador/atividades/inscritos.html`
- `README.md`
- `docs/validation-report.md` (este arquivo)
