# Registro de decisões

Os problemas e alternativas abaixo organizam decisões explícitas do [Contexto Mestre](project-context.md); não representam uma transcrição de discussões históricas.

| Pergunta/problema original | Alternativas | Decisão e justificativa | Impacto |
|---|---|---|---|
| Qual linha do framework? | Boot 3.x ou 4.x | 3.5.16: o enunciado exige 3.x. | Java 21 e dependências compatíveis; não usar 4.x. |
| Como entregar a interface? | MVC no servidor ou SPA | MVC/Thymeleaf: tecnologia obrigatória e escopo acadêmico. | Bootstrap 5 local, fragments, DTOs, validação e mensagens em português. |
| Onde vivem regras e persistência? | Controller ou camadas | Controller → service → repository: separação exigida. | Regras e transações nos services; controller não acessa repository. |
| Como evoluir esquema? | Hibernate update ou ferramenta de migração | `ddl-auto=update`, sem Flyway: escolha obrigatória. | Configuração em `application.yml`; sem cascata destrutiva. |
| Como representar perfis? | Roles múltiplas simultâneas ou exclusivas | ManyToMany com exatamente um papel funcional por usuário: ambos exigidos. | ADMIN e ORGANIZADOR são exclusivos; fluxo de concessão/retirada mantém invariável. |
| São necessários prefixos de perfil? | Somente roles ou prefixos explícitos | `/admin` e `/organizador` obrigatórios por RS04, sem `/participante`. | SecurityFilterChain protege prefixos; UI não substitui autorização. |
| Quem pode alterar um evento? | Qualquer organizador ou proprietário | Somente o organizador titular: exigência de segurança. | URL alterada para recurso alheio retorna 403. |
| Como transicionar eventos? | Publicação livre, reabertura ou ciclo restrito | DRAFT inicial, publicação com atividade, encerramento manual de PUBLISHED, sem reabertura. | Bloqueios de edição/exclusão e catálogo seguem status. |
| Como retirar ORGANIZADOR com eventos? | Retirar imediatamente ou proteger eventos | Bloquear se PUBLISHED; encerrar DRAFT antes de retirar; não alterar ADMIN. | Exceção específica DRAFT → CLOSED sem reabertura. |
| Como representar carga e vagas? | Derivar carga da duração ou informá-la separadamente; persistir contadores ou calcular vagas | Por solicitação posterior do usuário, calcular e persistir a duração em minutos; calcular inscritos/vagas. | Validar duração e capacidade; por decisão posterior do usuário, não usar locks explícitos e aceitar o risco de concorrência. |
| Como oferecer certificado? | Entidade própria ou documento derivado | Sem entidade Certificate; HTML imprimível e PDF OpenHTMLtoPDF. | Consultar presenças do usuário e somar cargas para evento CLOSED. |
| Qual cobertura automatizada? | Automatizar todas as regras ou escopo restrito | Somente RN01, RN02, RN04 com JUnit 5/Mockito sem PostgreSQL: EX04 delimitado. | Demais verificações são manuais e registradas; proteção contra concorrência está fora do escopo. |
| O que fazer nos instantes exatos da atividade? | Intervalo fechado ou início inclusivo e fim exclusivo | Início inclusivo e fim exclusivo, interpretação operacional dos períodos do contexto. | No início presença é permitida e cancelamento bloqueado; no fim os três são bloqueados. |
| Incluir espera e conflito de horários? | Incluir ou manter fora | Não implementar: contexto limita o escopo. | Nenhuma fila de espera ou validação de conflito. |
| Como entregar? | Documentação mínima ou execução reprodutível | README com Compose primeiro, alternativa Maven e dados de acesso; vídeo e tag finais solicitados no objetivo. | Criar issue, commit, tag, release ou vídeo apenas mediante pedido explícito. |
