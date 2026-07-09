# Notification Service

> Microserviço de notificações assíncronas construído com Kotlin, Spring Boot e RabbitMQ.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue?logo=kotlin)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange?logo=rabbitmq)](https://www.rabbitmq.com)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org)

---

## O que é esse projeto

Notification Service é um microserviço que recebe eventos de notificação de sistemas externos e entrega mensagens de forma confiável em múltiplos canais — **Email**, **WhatsApp** e **Webhook** — usando arquitetura orientada a eventos.

Qualquer sistema envia um único `POST /notifications`. O serviço cuida de encontrar os destinatários certos, aplicar o template correto, selecionar o canal adequado para cada um, e tratar falhas com reenvio automático.

**Exemplo real:** uma escola precisa avisar todos os pais da Turma 3A que amanhã não haverá aula. O sistema da escola manda um único evento com o `templateId` e o `groupId`. O Notification Service aplica o template, personaliza com o nome de cada destinatário, e entrega via WhatsApp para quem prefere WhatsApp e via email para quem prefere email — automaticamente, para todos os membros do grupo.

```
Sistema externo (escola, NGO, cantina, e-commerce...)
              │
              ▼
    POST /notifications
              │
              ▼
       Fila RabbitMQ
              │
              ├──▶ Email   (Mailhog / SMTP)
              ├──▶ WhatsApp (Twilio)
              └──▶ Webhook  (HTTP callbacks)
```

---

## Funcionalidades

- **Multi-tenant** — cada cliente tem sua própria API key e dados isolados
- **Templates com variáveis** — templates reutilizáveis com interpolação `{{variavel}}`
- **Personalização automática** — `{{nome}}`, `{{email}}` e `{{phone}}` injetados automaticamente por destinatário
- **Grupos de destinatários** — envie para um grupo e o serviço resolve todos os membros
- **Preferências por canal** — cada destinatário escolhe como quer ser notificado
- **Entrega assíncrona** — resposta `202 Accepted` imediata, entrega acontece em background
- **Retry com backoff exponencial** — 5 tentativas com delays crescentes (2s, 4s, 8s, 16s, 32s)
- **Dead-letter queue** — mensagens que esgotaram tentativas são preservadas para investigação
- **Reprocessamento da DLQ** — endpoint administrativo para reprocessar mensagens com falha
- **Rastreamento de entregas** — cada tentativa é persistida com status e detalhes de erro
- **Observabilidade** — métricas com Micrometer, Prometheus e Grafana

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.2 |
| Framework | Spring Boot 4.0 |
| Message Broker | RabbitMQ 3.13 |
| Banco de dados | PostgreSQL 16 |
| Migrations | Flyway |
| Email (local) | Mailhog |
| WhatsApp | Twilio WhatsApp Sandbox |
| Métricas | Micrometer + Prometheus + Grafana |
| Testes | JUnit 5 + MockK + Testcontainers |
| Infraestrutura | Docker Compose |

---

## Arquitetura

Este projeto segue a **Arquitetura Hexagonal** (Ports and Adapters), mantendo as regras de negócio completamente isoladas de detalhes externos como banco de dados, HTTP e mensageria.

### O princípio central

**O domínio não depende de nada. Todo o resto depende do domínio.**

```
Infraestrutura (JPA, RabbitMQ, HTTP, Email, Twilio)
          │
          │ depende de
          ▼
      Domínio (regras de negócio)
```

### As três camadas

**Domínio** — modelos e interfaces (portas), sem dependências de framework.

```
domain/
├── model/          ← data classes puras
├── port/
│   ├── input/      ← casos de uso (interfaces)
│   └── output/     ← repositórios e canais (interfaces)
└── service/        ← lógica de domínio (TemplateRenderer)
```

**Aplicação** — implementa os casos de uso. Orquestra o domínio, não conhece JPA nem HTTP.

```
application/usecase/
├── SendNotificationService.kt
├── DeliverNotificationService.kt
├── CreateTenantService.kt
├── CreateRecipientService.kt
├── CreateGroupService.kt
├── AddGroupMemberService.kt
├── CreateTemplateService.kt
├── FindNotificationService.kt
├── ListNotificationsService.kt
├── ListTemplatesService.kt
├── ListRecipientsService.kt
└── ListGroupsService.kt
```

**Infraestrutura** — adapters que implementam as portas usando tecnologia real.

```
infrastructure/
├── channel/        ← EmailChannel, WhatsAppChannel, WebhookChannel
├── config/         ← RestTemplateConfig
├── messaging/      ← RabbitMQ config, publisher, consumer
├── metrics/        ← NotificationMetrics
├── persistence/    ← JPA entities e repository adapters
└── web/
    ├── controller/ ← REST controllers
    ├── dto/        ← request/response bodies
    └── handler/    ← GlobalExceptionHandler
```

### Design Patterns aplicados

| Pattern | Onde | Propósito |
|---|---|---|
| **Strategy** | `NotificationChannelPort` | Cada canal de entrega é intercambiável — adicionar novo canal = criar nova classe |
| **Chain of Responsibility** | API Layer | Auth → Validação → Publicação |
| **Observer** | RabbitMQ consumer | Workers reagem a eventos sem acoplamento |
| **Template Method** | Retry logic | Mesmo fluxo, entrega diferente por canal |

---

## Canais de entrega

### Email
Entrega via SMTP. Em desenvolvimento usa **Mailhog** — servidor de email local com interface web para visualizar os emails enviados.

### WhatsApp
Integração real com **Twilio WhatsApp Sandbox**. Credenciais configuradas via variáveis de ambiente `TWILIO_ACCOUNT_SID` e `TWILIO_AUTH_TOKEN`.

### Webhook
Chama qualquer URL HTTP via POST com o payload estruturado em JSON. Ideal para integração entre sistemas — o sistema receptor processa o evento programaticamente.

```json
{
  "recipientId": "uuid",
  "recipientName": "Sistema da Cozinha",
  "payload": {
    "pedido": "2x Esfira de carne",
    "mesa": "5"
  }
}
```

> Nota: quando existe um template, ele é aplicado apenas para Email e WhatsApp. O Webhook sempre recebe o payload original estruturado.

---

## Templates com variáveis

Templates são cadastrados por tenant e canal, com suporte a interpolação de variáveis `{{variavel}}`.

**Variáveis automáticas** — injetadas pelo serviço antes da renderização:

| Variável | Valor |
|---|---|
| `{{nome}}` | Nome do destinatário |
| `{{email}}` | Email do destinatário |
| `{{phone}}` | Telefone do destinatário |

**Variáveis customizadas** — passadas no payload da notificação:

```json
{
  "templateId": "uuid-do-template",
  "payload": {
    "escola": "Escola Municipal Centro",
    "mensagem": "amanhã não haverá aula"
  }
}
```

Template: `"Olá {{nome}}, {{mensagem}}. Atenciosamente, {{escola}}."`

Resultado para João: `"Olá João da Silva, amanhã não haverá aula. Atenciosamente, Escola Municipal Centro."`

---

## Retry com Backoff Exponencial

Quando uma entrega falha, o serviço agenda reenvios automáticos com delays crescentes usando filas RabbitMQ com TTL fixo:

```
Tentativa 1 → falhou → notifications.retry.2s  (aguarda 2s)
Tentativa 2 → falhou → notifications.retry.4s  (aguarda 4s)
Tentativa 3 → falhou → notifications.retry.8s  (aguarda 8s)
Tentativa 4 → falhou → notifications.retry.16s (aguarda 16s)
Tentativa 5 → falhou → notifications.retry.32s (aguarda 32s) → DLQ
```

Mensagens na DLQ podem ser reprocessadas via `POST /admin/dlq/reprocess` após o problema ser resolvido.

---

## Observabilidade

O serviço expõe métricas via `/actuator/prometheus`, coletadas pelo Prometheus e visualizadas no Grafana.

| Métrica | Descrição |
|---|---|
| `notifications_sent_total` | Total de notificações enviadas |
| `notifications_delivered_total{channel}` | Entregas por canal |
| `notifications_failed_total{channel}` | Falhas por canal |
| `notifications_retry_total` | Total de retries |
| `notifications_dlq_total` | Total enviado para DLQ |
| `notifications_delivery_duration_seconds{channel}` | Tempo de entrega por canal |

**Serviços de monitoramento:**
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

---

## API

### Tenants

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/tenants` | Cria tenant e gera API key |

```json
// POST /tenants
{ "name": "Escola Municipal Centro" }

// 201 Created
{
  "id": "uuid",
  "name": "Escola Municipal Centro",
  "apiKey": "a3f8c2d1e4b5a6f7c8d9e0f1a2b3c4d5",
  "status": "ACTIVE"
}
```

### Recipients

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/recipients` | Cria destinatário |
| `GET` | `/recipients` | Lista destinatários do tenant |

```json
// POST /recipients
{
  "tenantId": "uuid",
  "name": "João da Silva",
  "email": "joao@escola.com",
  "phone": "+5541999999999",
  "webhookUrl": "https://seu-sistema.com/webhook",
  "channelPreferences": ["EMAIL", "WHATSAPP", "WEBHOOK"]
}
```

### Groups

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/groups` | Cria grupo |
| `GET` | `/groups` | Lista grupos do tenant |
| `POST` | `/groups/{id}/members` | Adiciona recipient ao grupo |

### Templates

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/templates` | Cria template |
| `GET` | `/templates` | Lista templates do tenant |

```json
// POST /templates
{
  "tenantId": "uuid",
  "name": "Aviso Escolar",
  "channel": "EMAIL",
  "subject": "Aviso — {{escola}}",
  "body": "Olá {{nome}}, {{mensagem}}. Atenciosamente, {{escola}}."
}
```

### Notifications

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/notifications` | Envia notificação |
| `GET` | `/notifications` | Lista notificações paginadas |
| `GET` | `/notifications/{id}` | Consulta status com isolamento por tenant |

```json
// POST /notifications
// Header: X-API-Key: {api-key}
{
  "templateId": "uuid-do-template",
  "groupId": "uuid-do-grupo",
  "payload": {
    "escola": "Escola Municipal Centro",
    "mensagem": "amanhã não haverá aula"
  }
}

// 202 Accepted
{ "id": "uuid", "status": "PENDING" }
```

### Admin — DLQ

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/admin/dlq/messages` | Lista mensagens na DLQ |
| `POST` | `/admin/dlq/reprocess` | Move mensagens de volta para a fila principal |

### Erros padronizados

Todas as respostas de erro seguem o mesmo formato:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid API key",
  "path": "/notifications",
  "timestamp": "2026-06-15T20:00:00Z"
}
```

| Status | Quando |
|---|---|
| `400` | Argumentos inválidos, tenant não encontrado |
| `401` | API key inválida |
| `403` | Recurso pertence a outro tenant |
| `404` | Recurso não encontrado |
| `422` | Validação de campos falhou |
| `500` | Erro inesperado |

---

## Rodando localmente

**Pré-requisitos:** Docker Desktop, Java 21

```bash
# 1. Clone o repositório
git clone https://github.com/Thiago-lemes/notification-service.git
cd notification-service

# 2. Suba a infraestrutura
docker-compose up -d

# 3. Configure as variáveis de ambiente no IntelliJ (Run Configurations)
# TWILIO_ACCOUNT_SID=seu-account-sid
# TWILIO_AUTH_TOKEN=seu-auth-token

# 4. Rode a aplicação
./mvnw spring-boot:run
```

**Serviços disponíveis:**

| Serviço | URL | Credenciais |
|---|---|---|
| API | http://localhost:8080 | — |
| RabbitMQ Management | http://localhost:15672 | guest/guest |
| Mailhog (email UI) | http://localhost:8025 | — |
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin/admin |

**Tenant de demonstração** criado automaticamente via migration:

```
Nome: EduManager Demo
API Key: demo-api-key-001
```

---

## Testes

```bash
./mvnw test
```

O projeto tem dois níveis de teste:

**Testes unitários** — cobrem todos os casos de uso com MockK. Sem Spring, sem banco, sem broker. Rodam em milissegundos.

**Testes de integração** — cobrem os adapters JPA com PostgreSQL real via Testcontainers. Validam queries, paginação e joins complexos contra um banco real.

```
application/usecase/
├── SendNotificationServiceTest      (4 testes)
├── CreateTenantServiceTest          (5 testes)
├── DeliverNotificationServiceTest   (9 testes)
├── FindNotificationServiceTest      (4 testes)
├── CreateRecipientServiceTest       (5 testes)
├── CreateGroupServiceTest           (4 testes)
└── AddGroupMemberServiceTest        (5 testes)

infrastructure/persistence/
├── TenantRepositoryAdapterTest      (4 testes)
├── NotificationRepositoryAdapterTest (4 testes)
└── RecipientRepositoryAdapterTest   (3 testes)
```

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
│   │   ├── RecipientGroup.kt
│   │   ├── RenderedTemplate.kt
│   │   ├── Template.kt
│   │   └── Tenant.kt
│   ├── port/
│   │   ├── input/
│   │   └── output/
│   └── service/
│       └── TemplateRenderer.kt
├── application/
│   └── usecase/
├── infrastructure/
│   ├── channel/
│   │   ├── EmailChannel.kt
│   │   ├── WhatsAppChannel.kt
│   │   └── WebhookChannel.kt
│   ├── config/
│   ├── messaging/
│   ├── metrics/
│   ├── persistence/
│   └── web/
└── NotificationServiceApplication.kt
```

---

## Architectural Decision Records

Todas as decisões arquiteturais significativas estão documentadas em [`/docs/adr`](./docs/adr).

| #                                                   | Decisão | Status |
|-----------------------------------------------------|---|---|
| [ADR-001](./docs/adr/001-hexagonal-architecture.md) | Arquitetura Hexagonal | Aceito |
| [ADR-002](./docs/adr/002-rabbitmq-async-delivery.md) | Entrega assíncrona com RabbitMQ | Aceito |
| [ADR-003](./docs/adr/003-strategy-pattern-channels.md) | Padrão Strategy para canais | Aceito |
| [ADR-004](./docs/adr/004-flyway-migrations.md)      | Migrations com Flyway | Aceito |
| [ADR-005](./docs/adr/005-retry-backoff-exponencial.md) | Retry com Backoff Exponencial | Aceito |

---

## Licença

MIT