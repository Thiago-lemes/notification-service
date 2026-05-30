# ADR-003: Padrão Strategy para canais de entrega

- **Status:** Aceito
- **Data:** 2026-04

---

## Contexto

O serviço precisa suportar múltiplos canais de entrega (Email, WhatsApp, Webhook) e crescer para novos canais no futuro sem que isso exija modificar o código existente. Precisávamos de uma forma de selecionar o canal correto em tempo de execução baseado na preferência de cada destinatário.

## Decisão

Adotamos o **padrão Strategy** através da interface `NotificationChannelPort`:

```kotlin
interface NotificationChannelPort {
    fun supports(channel: String): Boolean
    fun deliver(recipient: Recipient, payload: Map<String, Any>)
}
```

Cada canal implementa essa interface de forma independente:

- `EmailChannel` — entrega via SMTP / Mailhog
- `WhatsAppChannel` — entrega via Twilio WhatsApp Sandbox

O `DeliverNotificationService` recebe uma `List<NotificationChannelPort>` injetada pelo Spring e seleciona o canal correto em tempo de execução:

```kotlin
val channel = channels.find { it.supports(channelName) }
channel?.deliver(recipient, payload)
```

Adicionar um novo canal = criar uma nova classe que implementa `NotificationChannelPort`. Nenhuma linha existente precisa ser modificada. Isso segue o **Princípio Aberto/Fechado** do SOLID.

## Consequências

**Positivas:**
- Adicionar novos canais não modifica código existente
- Cada canal é testável de forma completamente isolada
- O `DeliverNotificationService` não precisa conhecer os canais — só a interface
- Canais podem ser ativados/desativados sem alterar o serviço

**Negativas:**
- Seleção por string (`"EMAIL"`, `"WHATSAPP"`) é menos type-safe do que um enum — mitigado com constantes
- Se nenhum canal suportar o tipo solicitado, a entrega é silenciosamente ignorada — requer log e monitoramento

## Alternativas consideradas

**Switch/when por tipo de canal:** descartado por violar o Princípio Aberto/Fechado — cada novo canal exigiria modificar o serviço.

**Enum com lógica interna:** descartado por misturar responsabilidades — o enum saberia entregar, o que dificulta testes e viola SRP.
