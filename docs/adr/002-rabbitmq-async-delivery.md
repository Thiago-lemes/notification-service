# ADR-002: Entrega assíncrona com RabbitMQ

- **Status:** Aceito
- **Data:** 2026-04

---

## Contexto

Entregar uma notificação para múltiplos destinatários via múltiplos canais pode ser lento e sujeito a falhas externas (SMTP indisponível, WhatsApp fora do ar). Fazer isso de forma síncrona dentro do request HTTP bloquearia o cliente e tornaria a API frágil.

## Decisão

A API aceita o evento e responde `202 Accepted` imediatamente. A entrega real acontece de forma assíncrona através do **RabbitMQ**.

Adotamos o padrão de **Dead Letter Queue (DLQ)**: mensagens que falham após todas as tentativas são movidas para uma fila separada (`notifications.dlq`) em vez de serem descartadas, permitindo investigação e reprocessamento manual via `POST /admin/dlq/reprocess`.

```
notifications.exchange  →  notifications.queue  →  [worker]
                                  │ falha 5x
                                  ▼
                    notifications.retry.{2s,4s,8s,16s,32s}
                                  │ esgota
                                  ▼
                        notifications.exchange.dlx  →  notifications.dlq
```

## Consequências

**Positivas:**
- API é resiliente a falhas de canais externos — o cliente não percebe
- Suporta picos de volume sem degradar o tempo de resposta
- DLQ garante que nenhuma mensagem seja perdida silenciosamente
- Retry automático com backoff sem lógica adicional na aplicação

**Negativas:**
- Entrega não é garantida em tempo real — há latência entre o `202` e a entrega efetiva
- Infraestrutura adicional (RabbitMQ) para operar e monitorar
- Debugging mais complexo — rastrear uma entrega exige consultar banco + fila

## Alternativas consideradas

**Entrega síncrona:** descartada pela fragilidade — uma falha no SMTP tornaria a API inteira indisponível.

**Kafka:** considerado, mas RabbitMQ é mais simples de operar para o volume esperado. Kafka seria justificado se houvesse necessidade de replay de eventos ou consumo por múltiplos sistemas independentes. No Kafka, o reprocessamento de mensagens com falha é nativo via consumer group offset reset — no RabbitMQ é necessário implementar a DLQ explicitamente.

**Spring Events (in-process):** descartado por não sobreviver a falhas de processo — se a aplicação cair durante a entrega, o evento se perde.
