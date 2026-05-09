# Notification Service

> Microserviço de notificações assíncronas construído com Kotlin, Spring Boot e RabbitMQ.

---

## O que é esse projeto

Notification Service é um microserviço que recebe eventos de notificação de sistemas externos e entrega mensagens de forma confiável em múltiplos canais — Email, WhatsApp e Webhooks — usando arquitetura orientada a eventos.

Qualquer sistema envia um único `POST /notifications`. O serviço cuida de encontrar os destinatários certos, selecionar o canal correto para cada um, e tratar falhas com reenvio automático.

**Exemplo real:** uma escola precisa avisar todos os pais que amanhã não haverá aula. O sistema da escola manda um único evento. O Notification Service entrega o aviso via WhatsApp para quem prefere WhatsApp, e via email para quem prefere email — automaticamente.

```
Sistema externo (escola, NGO, e-commerce...)
              │
              ▼
    POST /notifications
              │
              ▼
       Fila RabbitMQ
              │
              ├──▶ Email   (Mailhog / SMTP)
              ├──▶ WhatsApp (Evolution API)
              └──▶ Webhook  (HTTP callbacks)
```

---

## Funcionalidades

- **Multi-tenant** — cada cliente tem sua própria API key e dados isolados
- **Grupos de destinatários** — envie para um grupo e o serviço resolve todos os membros
- **Preferências por canal** — cada destinatário escolhe como quer ser notificado
- **Entrega assíncrona** — resposta `202 Accepted` imediata, entrega acontece em background
- **Idempotência** — header `Idempotency-Key` previne notificações duplicadas
- **Dead-letter queue** — mensagens com falha são preservadas para investigação
- **Rastreamento de entregas** — cada tentativa de entrega é persistida com status e detalhes de erro

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| Framework | Spring Boot 4.x |
| Message Broker | RabbitMQ |
| Banco de dados | PostgreSQL |
| Migrations | Flyway |
| Email (local) | Mailhog |
| WhatsApp | Evolution API |
| Infraestrutura | Docker Compose |

---

## Arquitetura

Este projeto segue a **Arquitetura Hexagonal**, também conhecida como *Ports and Adapters*. É um padrão arquitetural que mantém as regras de negócio completamente isoladas de detalhes externos como banco de dados, HTTP e mensageria.

### O problema que ela resolve

Em arquiteturas tradicionais em camadas, o código de negócio frequentemente depende diretamente de frameworks e infraestrutura:

```
Controller → Service → Repository (JPA) → Banco
```

Isso cria um problema: se você quiser trocar o banco de dados, reescrever uma lógica de negócio, ou escrever testes unitários sem subir o Spring, vai encontrar resistência. O negócio está acoplado à infraestrutura.

A Arquitetura Hexagonal inverte essa dependência.

### O princípio central

**O domínio não depende de nada. Todo o resto depende do domínio.**

```
Infraestrutura (JPA, RabbitMQ, HTTP, Email)
          │
          │ depende de
          ▼
      Domínio (regras de negócio)
```

O domínio não sabe que existe Spring. Não sabe que existe PostgreSQL. Não sabe que existe RabbitMQ. Ele só conhece suas próprias regras e contratos.

### As três camadas

#### 1. Domínio

O coração da aplicação. Contém os modelos de negócio e as interfaces que definem o que o domínio precisa do mundo externo.

```
domain/
├── model/          ← data classes puras, sem anotações de framework
│   ├── Notification.kt
│   ├── Recipient.kt
│   ├── Tenant.kt
│   └── NotificationDelivery.kt
└── port/
    ├── input/      ← o que a aplicação é capaz de fazer (casos de uso)
    │   ├── SendNotificationUseCase.kt
    │   └── SendNotificationRequest.kt
    └── output/     ← o que a aplicação precisa do mundo externo
        ├── SaveNotificationPort.kt
        ├── FindNotificationPort.kt
        ├── TenantRepository.kt
        └── NotificationChannelPort.kt
```

Os modelos de domínio são classes Kotlin puras — sem `@Entity`, sem `@Column`, sem nenhuma anotação de framework:

```kotlin
// Modelo de domínio puro — não sabe que existe JPA
data class Notification(
    val id: UUID = UUID.randomUUID(),
    val tenantId: UUID,
    val payload: Map<String, Any>,
    val status: String = "PENDING"
)
```

#### 2. Aplicação

Implementa os casos de uso. Orquestra os modelos de domínio e chama as portas de saída. Não sabe nada sobre HTTP ou JPA — só conhece interfaces.

```
application/
└── usecase/
    ├── SendNotificationService.kt    ← orquestra o POST /notifications
    └── DeliverNotificationService.kt ← orquestra o consumo da fila
```

#### 3. Infraestrutura

O mundo externo. Implementa as portas definidas pelo domínio usando tecnologia real: Spring Data JPA, RabbitMQ, JavaMailSender.

```
infrastructure/
├── persistence/    ← entidades JPA e adapters de repositório
├── messaging/      ← configuração RabbitMQ, publisher e consumer
├── channel/        ← EmailChannel, WhatsAppChannel
└── web/
    ├── controller/ ← controllers REST
    └── dto/        ← objetos de request e response
```

### Portas e Adapters na prática

**Porta** é uma interface que vive no domínio. Ela declara um contrato sem saber como será implementado.

**Adapter** é a implementação dessa interface na camada de infraestrutura.

Exemplo completo do fluxo de persistência:

```kotlin
// 1. PORTA — domínio declara o contrato (domain/port/output/)
interface SaveNotificationPort {
    fun save(notification: Notification): Notification
}

// 2. ADAPTER — infraestrutura implementa o contrato (infrastructure/persistence/)
@Component
class NotificationRepositoryAdapter(
    private val jpaRepository: NotificationJpaRepository
) : SaveNotificationPort {
    override fun save(notification: Notification): Notification {
        jpaRepository.save(notification.toEntity())
        return notification
    }
}

// 3. CASO DE USO — só conhece a interface, nunca a implementação (application/usecase/)
@Service
class SendNotificationService(
    private val saveNotification: SaveNotificationPort  // ← interface, não JPA
) : SendNotificationUseCase {
    override fun send(apiKey: String, request: SendNotificationRequest): Notification {
        // lógica de negócio aqui
        return saveNotification.save(notification)
    }
}
```

O Spring resolve a implementação em tempo de execução via injeção de dependência. O `SendNotificationService` nunca viu um `@Repository` na vida.

### Por que isso importa

| Benefício | Como aparece neste projeto |
|---|---|
| **Testabilidade** | `SendNotificationService` pode ser testado com um mock de `SaveNotificationPort`, sem subir banco |
| **Substituibilidade** | Trocar PostgreSQL por outro banco = reescrever só os adapters |
| **Clareza de intenção** | As interfaces de porta documentam exatamente o que o domínio precisa |
| **Isolamento de mudanças** | Mudar o formato do JSON da API não afeta o domínio |

### Design Patterns aplicados

| Pattern | Onde | Propósito |
|---|---|---|
| **Strategy** | `NotificationChannelPort` | Cada canal de entrega é intercambiável. Adicionar WhatsApp = criar nova classe |
| **Chain of Responsibility** | Pipeline da API Layer | Autenticação → Validação → Idempotência → Publicação |
| **Observer** | Consumer RabbitMQ | Workers reagem a eventos sem acoplamento com quem publicou |
| **Template Method** | Lógica de retry | Mesmo fluxo de retry, entrega diferente por canal |

---

## Fluxo completo

### Publicação

```
POST /notifications  (com X-API-Key e Idempotency-Key)
        │
        ▼
[1] Autenticação — valida api_key, identifica tenant → 401 se inválida
        │
        ▼
[2] Validação — template existe? grupo existe? payload ok? → 422 se inválido
        │
        ▼
[3] Idempotência — Idempotency-Key já foi processada? → 200 cached se sim
        │
        ▼
[4] Persiste no PostgreSQL com status PENDING
        │
        ▼
[5] Publica evento no RabbitMQ
        │
        ▼
202 Accepted {"id": "...", "status": "PENDING"}
```

### Entrega

```
RabbitMQ entrega mensagem ao consumer
        │
        ▼
[1] Busca Notification no banco pelo notificationId
        │
        ▼
[2] Busca destinatários do tenant
        │
        ▼
[3] Para cada destinatário, para cada canal preferido:
        │
        ├── channel.supports("EMAIL")?  → EmailChannel.deliver()
        ├── channel.supports("WHATSAPP")? → WhatsAppChannel.deliver()
        │
        ▼
[4] Sucesso → salva delivery com status DELIVERED
    Falha   → salva delivery com status FAILED + errorMessage
           → RabbitMQ faz retry automático
           → após N tentativas → mensagem vai para Dead Letter Queue
```

---

## API

### Enviar notificação

```
POST /notifications
X-API-Key: {sua-api-key}
Idempotency-Key: {uuid-unico}
Content-Type: application/json
```

```json
{
  "templateId": null,
  "groupId": null,
  "payload": {
    "subject": "Aviso escolar",
    "message": "Amanhã não haverá aula."
  }
}
```

**Resposta `202 Accepted`**
```json
{
  "id": "6688e852-717f-4dc9-89c1-95ca72c93539",
  "status": "PENDING"
}
```

**Resposta `401 Unauthorized`** — API key inválida
```json
{
  "error": "Invalid API key"
}
```

### Consultar status

```
GET /notifications/{id}
X-API-Key: {sua-api-key}
```

---

## Rodando localmente

**Pré-requisitos:** Docker Desktop, Java 21

```bash
# 1. Clone o repositório
git clone https://github.com/thiago/notification-service.git
cd notification-service

# 2. Suba a infraestrutura
docker-compose up -d

# 3. Rode a aplicação
./mvnw spring-boot:run
```

**Serviços disponíveis:**

| Serviço | URL |
|---|---|
| API | http://localhost:8080 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| Mailhog (UI de email) | http://localhost:8025 |

---

## Estrutura do projeto

```
src/main/kotlin/dev/thiago/notification_service/
├── domain/
│   ├── model/
│   │   ├── Notification.kt
│   │   ├── NotificationDelivery.kt
│   │   ├── NotificationEvent.kt
│   │   ├── Recipient.kt
│   │   └── Tenant.kt
│   └── port/
│       ├── input/
│       │   ├── SendNotificationUseCase.kt
│       │   └── SendNotificationRequest.kt
│       └── output/
│           ├── SaveNotificationPort.kt
│           ├── FindNotificationPort.kt
│           ├── SaveDeliveryPort.kt
│           ├── FindRecipientsByTenantPort.kt
│           ├── TenantRepository.kt
│           └── NotificationChannelPort.kt
├── application/
│   └── usecase/
│       ├── SendNotificationService.kt
│       └── DeliverNotificationService.kt
└── infrastructure/
    ├── persistence/
    ├── messaging/
    ├── channel/
    └── web/
```

---

## Architectural Decision Records

Todas as decisões arquiteturais significativas estão documentadas em [`/docs/adr`](./docs/adr).

| # | Decisão | Status |
|---|---|---|
| [ADR-001](./docs/adr/001-hexagonal-architecture.md) | Arquitetura Hexagonal | Aceito |
| [ADR-002](./docs/adr/002-rabbitmq-async-delivery.md) | Entrega assíncrona com RabbitMQ | Aceito |
| [ADR-003](./docs/adr/003-strategy-pattern-channels.md) | Padrão Strategy para canais de entrega | Aceito |
| [ADR-004](./docs/adr/004-flyway-migrations.md) | Migrations com Flyway | Aceito |

---

## Licença

MIT