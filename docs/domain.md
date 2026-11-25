# Domínio

O modelo tem exatamente seis entidades JPA; `Certificate` não é entidade. Código e atributos usam inglês. O [Contexto Mestre](project-context.md) é a autoridade.

| Entidade | Atributos |
|---|---|
| User | `id`, `name`, `email` único, `password` BCrypt, `active` |
| Role | `id`, `name` |
| Category | `id`, `name` |
| Event | `id`, `title`, `description`, `location`, `startDate`, `endDate`, `status` |
| Activity | `id`, `title`, `type`, `instructor`, `startDateTime`, `endDateTime`, `workloadMinutes` Integer, `capacity` |
| Enrollment | `id`, `dateTime`, `present` |

## Relacionamentos e restrições

- User ↔ Role: ManyToMany; cada User tem exatamente um papel funcional exclusivo: `ROLE_PARTICIPANTE`, `ROLE_ORGANIZADOR` ou `ROLE_ADMIN`.
- Category 1 → N Event; cada Event pertence a uma Category. User organizador 1 → N Event; cada Event pertence a um organizador.
- Event 1 → N Activity; cada Activity pertence a um Event. Activity 1 → N Enrollment e User 1 → N Enrollment; cada Enrollment pertence a um User e a uma Activity. Par `(user_id, activity_id)` único.
- `EventStatus`: `DRAFT`, `PUBLISHED`, `CLOSED`; `ActivityType`: `LECTURE`, `MINICOURSE`, `WORKSHOP`. Persistir enums como String; apresentação pode ser em português.
- `endDate >= startDate`; `endDateTime > startDateTime` e atividade no período inclusivo do evento. Formulário de carga recebe horas e minutos, minutos 0–59, total positivo; persistir total em `workloadMinutes`, sem exigir igualdade à duração. Capacidade não pode ser reduzida abaixo do número de inscritos.
- Inscritos e vagas restantes são calculados; não persistir contadores. Sem cascata destrutiva. A remoção respeita as dependências descritas em [business-rules.md](business-rules.md).

## Transições

Event nasce DRAFT; DRAFT → PUBLISHED exige pelo menos uma atividade; PUBLISHED → CLOSED é manual. Não há reabertura. Exceção específica: ao retirar ORGANIZADOR, seus eventos DRAFT são encerrados antes da retirada, desde que não haja PUBLISHED. ADMIN não é alterado pelo fluxo. As restrições de edição, exclusão, inscrição e presença estão nas matrizes de [business-rules.md](business-rules.md).
