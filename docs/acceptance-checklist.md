# Checklist de aceitação

Registrar data, ambiente, resultado e evidência de cada verificação manual, inclusive falhas; usar usuários de cada papel e dados preparados. O mapeamento de identificadores está em [requirements.md](requirements.md), e as matrizes em [business-rules.md](business-rules.md). Esta lista não autoriza criar artefatos de entrega sem pedido explícito.

## Funcionais

- [ ] RF01: cadastrar publicamente e verificar papel PARTICIPANTE único.
- [ ] RF02: entrar por email/senha e sair; confirmar acesso após logout.
- [x] RF03: ADMIN cria, consulta, altera e remove categoria permitida.

  Verificação manual em 25/11/2026: aplicação Java 21 e PostgreSQL 16 em Docker Compose isolado; requisições HTTP e consulta ao banco. ADMIN consultou a listagem, criou e editou categoria, com mensagens flash e nomes confirmados no banco; excluiu uma categoria sem eventos e recebeu bloqueio com mensagem para categoria com evento, que permaneceu no banco. PARTICIPANTE e ORGANIZADOR receberam 403 em GET, POST, PUT e DELETE. Nome vazio foi recusado com erro de campo; GET de edição, PUT e DELETE com ID inexistente retornaram 404; POST sem CSRF retornou 403.
- [x] RF04: ADMIN lista usuários, concede e retira ORGANIZADOR; exercitar bloqueios e proteção de ADMIN.

  Verificação manual em 25/11/2026: aplicação Java 21 e PostgreSQL 16 em Docker Compose isolado, requisições HTTP e conferência no banco. A listagem retornou 200 para ADMIN; anônimo foi redirecionado e PARTICIPANTE/ORGANIZADOR receberam 403. Com CSRF válido, ambos receberam 403 em PUT e DELETE diretos; sem CSRF, ADMIN recebeu 403. Concessão e retirada sem eventos preservaram um único papel. Retirada com PUBLISHED e DRAFT ou CLOSED foi bloqueada sem alterar estados; com apenas DRAFT, com DRAFT e CLOSED, e com apenas CLOSED, concluiu com papel PARTICIPANTE e rascunhos encerrados. Tentativas de conceder e retirar papel de ADMIN foram bloqueadas e mostraram mensagem. As operações retornaram mensagens flash.

  Após concessão e retirada, a sessão aberta com o papel anterior foi invalidada na próxima requisição e redirecionada ao login.
- [x] RF05: ORGANIZADOR cria, consulta, altera e remove evento próprio permitido; tentar recurso alheio.

  Verificação manual em 25/11/2026: Java 21 e PostgreSQL 16 em Docker Compose isolado. ORGANIZADOR criou rascunho, editou DRAFT e PUBLISHED, excluiu DRAFT sem atividades e recebeu bloqueio com atividades ou em CLOSED. Publicação sem atividade foi bloqueada; com atividade, passou a PUBLISHED; somente PUBLISHED foi encerrado. Edição, publicação, encerramento ou exclusão de CLOSED foram bloqueados. Um segundo organizador recebeu 403 ao acessar, editar, publicar, encerrar ou excluir evento alheio; ADMIN e PARTICIPANTE receberam 403 na listagem. Data final anterior à inicial foi recusada, datas iguais foram aceitas e alteração que excluiria atividade do período foi bloqueada. Um evento PUBLISHED sem atividades foi preparado diretamente no banco isolado e pôde ser excluído; o fluxo normal de publicação exige atividade, portanto essa situação normalmente não ocorre. Testes JUnit/Mockito de RN02 em EventService: três aprovados.
- [x] RF06: ORGANIZADOR gerencia atividades próprias e exercita restrições de exclusão.

  Verificação manual em 25/11/2026: Java 21 e PostgreSQL 16 em Docker Compose isolado. Criou e editou atividades em DRAFT e PUBLISHED; criação, edição e exclusão em CLOSED foram bloqueadas. Em PUBLISHED, exclusão da última atividade foi bloqueada e a exclusão de uma atividade sem inscrições quando havia outra foi permitida; em DRAFT foi permitida a exclusão da última sem inscrições. Atividade com inscrições permaneceu no banco e apresentou mensagem de bloqueio. Capacidade igual ao número de inscritos foi aceita e capacidade inferior foi bloqueada. Datas fora do evento e término igual ou anterior ao início foram recusados; limites de data inclusivos foram exercitados nos testes de RN02. Carga de 1h35 foi armazenada como 95 minutos e apresentada convertida, independentemente da duração de duas horas; carga zero e minutos acima de 59 foram recusados. Outro ORGANIZADOR recebeu 403 em listagem, edição, criação, alteração e exclusão de evento alheio. Os testes JUnit/Mockito de RN02 em ActivityService e EventService passaram (seis no total).

  Alteração posterior solicitada pelo usuário: a carga passa a ser calculada pela duração. Verificação manual no ambiente isolado: 09:00–10:35 gerou 95 minutos e foi exibido como 1h35; alteração do término para 11:00 gerou 120 minutos. O formulário deixou de solicitar carga e passou a apresentar “Número de vagas”; duração com fração de minuto foi recusada. Os seis testes de RN02 passaram novamente. O registro anterior preserva o resultado da regra que estava vigente à época.
- [x] RF07: verificar DRAFT inicial, publicação com/sem atividade, encerramento e ausência de reabertura (ver registro de RF05).
- [x] RF08: conferir só publicados, filtro por categoria, 9 por página e ordem/data com desempate por id.

  Verificação manual em 26/11/2026: aplicação Java 21 e PostgreSQL 16 em Docker Compose isolado, com dados preparados no banco e requisições HTTP anônimas. Entre 12 eventos publicados, a primeira página apresentou os IDs 1, 3, 4, 5, 6, 7, 8, 9 e 10; a segunda apresentou 11, 12 e 13, em ordem de data e ID para datas iguais. O filtro da categoria 1 apresentou apenas 1, 4, 6, 8, 10 e 12. Rascunho e evento encerrado não apareceram.
- [x] RF09: detalhe publicado mostra atividades/vagas; URLs de DRAFT e CLOSED retornam 404.

  Verificação manual no mesmo ambiente: detalhe publicado retornou 200 com atividade de capacidade 30, um inscrito e 29 vagas restantes; visitante não recebeu estado pessoal de inscrição. Após login como PARTICIPANTE inscrito, o detalhe indicou a inscrição. IDs de evento encerrado, rascunho e inexistente retornaram 404.
- [x] RF10: inscrever e cancelar nos estados e momentos permitidos; conferir bloqueios.
- [x] RF11: conferir inscrições/presenças do usuário da sessão, sem acesso às de terceiros.
- [x] RF12: organizador lista inscritos e marca presença nos momentos permitidos; testar recurso alheio.

  Verificação manual em 26/11/2026: Java 21 e PostgreSQL 16 em Docker Compose isolado; dados e horários preparados no banco, requisições HTTP com sessões e CSRF. Inscrição em DRAFT/CLOSED e após término foi recusada; antes e durante foi aceita. Duplicata foi recusada. Cancelamento próprio antes foi aceito; durante, após término e em CLOSED foi recusado; tentativa sobre inscrição alheia retornou 403. O dono listou inscritos e registrou presença durante; outro organizador recebeu 403 para lista e alteração. Presença antes, após término e em CLOSED foi recusada; a presença registrada apareceu em Minhas inscrições. Participante recebeu 403 na organização e organizador recebeu 403 em /inscricoes. Em duas requisições simultâneas de participantes distintos para capacidade 1, uma inscrição foi aceita e a outra recusada; o banco confirmou um inscrito. Roteiro: [FT07](ft07-validacao-manual.md).
- [ ] RF13: usuário elegível acessa HTML imprimível e PDF com nome, evento e soma correta; inelegível é bloqueado.
- [ ] RF14: executar seed mais de uma vez e conferir duas categorias, dois eventos com atividades e usuário de cada perfil sem duplicações.

## Regras de negócio

- [ ] RN01: tentar email repetido; executar teste unitário de UserService.
- [ ] RN02: testar limites e datas inválidas de evento/atividade, inclusive limites inclusivos; executar testes de EventService e ActivityService.
- [x] RN03: tentar inscrição em DRAFT, PUBLISHED e CLOSED (ver registro de RF10–RF12).
- [x] RN04: tentar inscrição repetida; executar teste de EnrollmentService (ver registro de RF10–RF12).
- [ ] RN05: verificar sequencialmente a capacidade e a recusa de inscrição quando estiver cheia. Proteção contra inscrições simultâneas está fora do escopo; a capacidade pode ser ultrapassada em concorrência. O resultado simultâneo registrado em RF10–RF12 é histórico e antecede a retirada dos locks.
- [ ] RN06: cancelar antes, no início, durante e após término; conferir bloqueio em CLOSED.
- [ ] RN07: testar evento aberto/fechado e ausência/presença registrada, incluindo soma apenas das cargas com presença.
- [ ] Conferir início inclusivo/fim exclusivo nas três operações, bloqueio de alteração em CLOSED, revogação de organizador com DRAFT/PUBLISHED, exclusões e ausência de cascata destrutiva.

## Segurança, interface e técnica

- [ ] RS01: login por email e senha; verificar BCrypt e serviço de autenticação.
- [ ] RS02: testar acesso anônimo às rotas públicas e restrição das demais.
- [ ] RS03: testar exclusividade ORGANIZADOR e 403 ao alterar URL para evento/atividade alheio.
- [ ] RS04: confirmar `/admin` e `/organizador` e inexistência de `/participante`.
- [ ] RS05: testar exclusividade ADMIN e um papel funcional por usuário.
- [ ] RS06: manipular identificador em solicitação e confirmar uso da sessão em inscrições/certificados.
- [ ] RS07: testar acesso direto mesmo quando o menu estiver oculto.
- [ ] RI01: conferir renderização server-side, fragments e Bootstrap local.
- [ ] RI02: conferir DTOs, Bean Validation, `th:object`/`th:field` e erros por campo.
- [ ] RI03: conferir português em rotas, telas e mensagens e flash messages.
- [ ] RT01: verificar versões e tecnologias obrigatórias.
- [ ] RT02: inspecionar pacote, camadas, inglês no código e regras/transações nos services.
- [ ] RT03: inspecionar Dockerfile, Compose, `application.yml`, Hibernate update e dependências proibidas.
- [ ] RT04: verificar verbos HTTP e PUT/DELETE de formulários via filtro.
- [ ] RT05: inspecionar seis entidades, enums String, restrições de unicidade e ausência de cascata destrutiva.
- [ ] EX03: abrir HTML imprimível e PDF OpenHTMLtoPDF e conferir dados.
- [x] EX04: executar apenas testes automatizados previstos com JUnit 5/Mockito sem PostgreSQL; conferir que regras excluídas são verificadas manualmente e registradas.

  Verificação em 27/11/2026: `mvn test` em contêiner `maven:3.9-eclipse-temurin-21`, sem PostgreSQL. Classes: `UserServiceTest` (RN01, 1), `EventServiceTest` (RN02, 3), `ActivityServiceTest` (RN02, 5) e `EnrollmentServiceTest` (RN04, 1, com `Clock` fixo). Resultado: 10 testes, 0 falhas, 0 erros. Os testes conferem a mensagem da regra violada e que nada é salvo. Não há testes automatizados de RN03, RN05, RN06, RN07, concorrência, controllers, segurança ou PDF.

## Entrega

- [ ] README reprodutível: Compose primeiro, alternativa Maven, URL, variáveis de ambiente, pré-requisitos, credenciais dos três perfis, estrutura, extras e pendências.
- [ ] `.gitignore` exclui `target`, `build`, `.idea` e segredos.
- [ ] Repositório pronto; vídeo de até 10 minutos demonstra fluxos dos perfis, 403 por URL alterada, entidades, segurança e services.
- [ ] Tag final planejada; não criar issue, commit, tag, release ou vídeo sem pedido explícito.
