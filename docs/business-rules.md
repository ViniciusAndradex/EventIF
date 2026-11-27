# Regras e matrizes

[Contexto Mestre](project-context.md) prevalece. Operações também exigem identidade, papel e titularidade apropriados.

## Status do evento

| Operação | DRAFT | PUBLISHED | CLOSED |
|---|---|---|---|
| Catálogo público/detalhe | 404 | Visível | 404 |
| Editar evento ou atividades | Permitido | Permitido | Bloqueado |
| Publicar | Com ao menos uma atividade | Não aplicável | Bloqueado |
| Encerrar manualmente | Não permitido | Permitido | Sem reabertura |
| Excluir evento | Apenas sem dependências | Apenas sem dependências | Nunca |
| Excluir atividade | Apenas sem inscrições | Apenas sem inscrições e não sendo a última | Nunca |
| Inscrição, cancelamento e presença | Inscrição bloqueada; demais sujeitos às regras | Conforme matriz temporal | Todos bloqueados |
| Certificado | Não | Não | Com ao menos uma presença do usuário |

Ao retirar ORGANIZADOR, bloquear se houver eventos PUBLISHED; se houver somente DRAFT, encerrá-los e depois retirar o papel. Essa transição DRAFT → CLOSED é específica desse fluxo e não é encerramento manual. Não alterar ADMIN nesse fluxo. Não usar cascata destrutiva.

## Tempo da atividade

| Momento | Inscrição | Cancelamento | Presença |
|---|---|---|---|
| Antes do início | Permitida se PUBLISHED, houver vaga e não houver duplicata | Permitido, se evento não CLOSED | Bloqueada |
| Durante | Permitida se PUBLISHED, houver vaga e não houver duplicata | Bloqueado | Permitida se evento não CLOSED |
| Após término | Bloqueada | Bloqueado | Bloqueada |

Para tornar os limites operacionais, considera-se durante `startDateTime <= agora < endDateTime`; no instante do término aplicam-se os bloqueios posteriores. A matrícula exige PUBLISHED em qualquer momento. O encerramento bloqueia todas as alterações, inclusive inscrição, cancelamento e presença. `Clock` injetável quando necessário; verificar a capacidade antes de gravar a inscrição, sem lock explícito; inscrições simultâneas podem ultrapassar a capacidade.

## Validações

| Tema | Condição |
|---|---|
| Email | Único (RN01). |
| Datas | Fim do evento >= início; fim da atividade > início; atividade dentro das datas inclusivas do evento (RN02). |
| Inscrição | Evento PUBLISHED (RN03), sem duplicata por usuário/atividade (RN04), capacidade verificada antes da inscrição (RN05; sem garantia sob concorrência), respeitando tempo. |
| Cancelamento | Antes do início (RN06) e evento não CLOSED. |
| Certificado | Evento CLOSED e ao menos uma presença do usuário (RN07); carga é a soma das atividades com presença. |
| Publicação | Pelo menos uma atividade. |
| Carga horária | Calculada pela duração entre início e término, em minutos inteiros positivos, e persistida. |
| Capacidade | Não reduzir abaixo dos inscritos; vagas e inscritos calculados. |
| Proprietário | Ação sobre evento/atividade alheio por organizador retorna 403. |

Não implementar lista de espera nem detecção de conflitos de horários.
