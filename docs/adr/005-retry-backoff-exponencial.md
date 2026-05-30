# ADR-005: Retry com Backoff Exponencial

- **Status:** Aceito
- **Data:** 2026-05

---

## Contexto

Canais de entrega externos (SMTP, WhatsApp, Webhooks) podem falhar temporariamente por indisponibilidade, timeout ou
rate limiting. Sem uma estratégia de retry, notificações seriam perdidas em caso de falha transitória.

## Decisão

Implementamos retry com **backoff exponencial** usando filas RabbitMQ com TTL dinâmico.

O fluxo é:

1. Mensagem falha na `notifications.queue`
2. É enviada para `notifications.retry.queue` com TTL calculado
3. Após o TTL expirar, volta para `notifications.queue`
4. Após 5 tentativas, vai para `notifications.dlq`

O delay entre tentativas dobra a cada falha:

- Tentativa 1 → 2s
- Tentativa 2 → 4s
- Tentativa 3 → 8s
- Tentativa 4 → 16s
- Tentativa 5 → 32s → DLQ

## Consequências

**Positivas:**

- Falhas transitórias são resolvidas automaticamente
- Sem sobrecarga dos serviços externos com retries imediatos
- Mensagens nunca perdidas — DLQ preserva para investigação
- Delay crescente dá tempo para serviços se recuperarem

**Negativas:**

- Entrega pode demorar até 62s no pior caso antes de ir para DLQ
- Complexidade adicional no consumer
- Necessidade de monitorar a DLQ ativamente

## Alternativas consideradas

**Retry imediato:** descartado — sobrecarrega serviços externos e não resolve falhas transitórias.

**Spring Retry:** considerado, mas não sobrevive a falhas de processo — se a aplicação cair durante o retry, a mensagem
se perde. RabbitMQ garante durabilidade.