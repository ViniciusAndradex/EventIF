# FT08 — validação manual

Validação em 26/11/2026 com aplicação e PostgreSQL isolados, participantes diferentes e requisições HTTP autenticadas:

- Sem presença: lista vazia; HTML e PDF do evento retornam 404.
- Evento publicado com presença: não aparece na lista; HTML e PDF retornam 404.
- Evento encerrado com duas presenças de 120 e 35 minutos e uma inscrição sem presença de 45 minutos: aparece na lista; HTML e PDF exibem participante, evento e 2 horas e 35 minutos.
- Outro participante sem presença no evento: HTML e PDF retornam 404 mesmo informando o identificador do evento.
- PDF: resposta `application/pdf`, apresentação inline e conteúdo textual conferido com `pdftotext`.
- Impressão: HTML contém botão de impressão e regra CSS para ocultar ações na impressão. A pré-visualização e a impressão visual em navegador não foram verificadas neste ambiente.
