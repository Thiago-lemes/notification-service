# Notification Service

> Microserviço de notificações assíncronas construído com Kotlin, Spring Boot e RabbitMQ.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue?logo=kotlin)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange?logo=rabbitmq)](https://www.rabbitmq.com)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org)

---

## O que é esse projeto

Notification Service é um microserviço que recebe eventos de notificação de sistemas externos e entrega mensagens de forma confiável em múltiplos canais — **Email**, **WhatsApp** e **Webhooks** — usando arquitetura orientada a eventos.

Qualquer sistema envia um único `POST /notifications`. O serviço cuida de encontrar os destinatários certos, aplicar o template correto, selecionar o canal adequado para cada um, e tratar falhas com reenvio automático.

**Exemplo real:** uma escola precisa avisar todos os pais da Turma 3A que amanhã não haverá aula. O sistema da escola manda um único evento com o templateId e o groupId. O Notification Service aplica o template, e entrega o aviso via WhatsApp para quem prefere WhatsApp e via email para quem prefere email — automaticamente, para todos os membros do grupo.

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
- **Grupos de destinatários** — envie para um grupo e o serviço resolve todos os membros
- **Preferências por canal** — cada destinatário escolhe como quer ser notificado
- **Entrega assíncrona** — resposta `202 Accepted` imediata, entrega acontece em background
- **Retry com backoff exponencial** — 5 tentativas com delays crescentes (2s, 4s, 8s, 16s, 32s)
- **Dead-letter queue** — mensagens que esgotaram tentativas são preservadas para investigação
- **Reprocessamento da DLQ** — endpoint administrativo para reprocessar mensagens com falha
- **Rastreamento de entregas** — cada tentativa é persistida com status e detalhes de erro

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
| Testes | JUnit 5 + MockK |
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
└── FindNotificationService.kt
```

**Infraestrutura** — adapters que implementam as portas usando tecnologia real.

```
infrastructure/
├── persistence/    ← JPA entities e repository adapters
├── messaging/      ← RabbitMQ config, publisher, consumer
├── channel/        ← EmailChannel, WhatsAppChannel
└── web/
    ├── controller/ ← REST controllers
    └── dto/        ← request/response bodies
```

### Design Patterns aplicados

| Pattern | Onde | Propósito |
|---|---|---|
| **Strategy** | `NotificationChannelPort` | Cada canal de entrega é intercambiável |
| **Chain of Responsibility** | API Layer | Auth → Validação → Publicação |
| **Observer** | RabbitMQ consumer | Workers reagem a eventos sem acoplamento |
| **Template Method** | Retry logic | Mesmo fluxo, entrega diferente por canal |

---

## Fluxo completo

### Publicação

```
POST /notifications
        │
        ▼
[1] Autenticação — valida X-API-Key, identifica tenant → 401 se inválida
        │
        ▼
[2] Persiste no PostgreSQL com status PENDING
        │
        ▼
[3] Publica evento no RabbitMQ
        │
        ▼
202 Accepted {"id": "...", "status": "PENDING"}
```

### Entrega

```
RabbitMQ entrega mensagem ao consumer
        │
        ▼
[1] Busca Notification no banco
        │
        ▼
[2] Resolve destinatários — por grupo ou por tenant
        │
        ▼
[3] Aplica template se templateId presente
        │
        ▼
[4] Para cada destinatário, para cada canal preferido:
        ├── Sucesso → status DELIVERED
        └── Falha   → retry com backoff exponencial
                         └── após 5 tentativas → DLQ
```

---

## API

### Tenants

#### Criar tenant
```
POST /tenants
Content-Type: application/json
```
```json
{
  "name": "Escola Municipal Centro"
}
```
**Resposta `201 Created`**
```json
{
  "id": "uuid",
  "name": "Escola Municipal Centro",
  "apiKey": "a3f8c2d1e4b5a6f7c8d9e0f1a2b3c4d5",
  "status": "ACTIVE"
}
```

---

### Recipients

#### Criar destinatário
```
POST /recipients
Content-Type: application/json
```
```json
{
  "tenantId": "uuid-do-tenant",
  "name": "João da Silva",
  "email": "joao@escola.com",
  "phone": "+5541999999999",
  "channelPreferences": ["EMAIL", "WHATSAPP"]
}
```

---

### Groups

#### Criar grupo
```
POST /groups
Content-Type: application/json
```
```json
{
  "tenantId": "uuid-do-tenant",
  "name": "Turma 3A",
  "description": "Pais e responsáveis da turma 3A"
}
```

#### Adicionar membro ao grupo
```
POST /groups/{groupId}/members?recipientId={recipientId}
```

---

### Templates

#### Criar template
```
POST /templates
Content-Type: application/json
```
```json
{
  "tenantId": "uuid-do-tenant",
  "name": "Aviso Escolar",
  "channel": "EMAIL",
  "subject": "Aviso — {{escola}}",
  "body": "Olá {{responsavel}}, informamos que {{mensagem}}. Atenciosamente, {{escola}}."
}
```

---

### Notifications

#### Enviar notificação
```
POST /notifications
X-API-Key: {sua-api-key}
Content-Type: application/json
```
```json
{
  "templateId": "uuid-do-template",
  "groupId": "uuid-do-grupo",
  "payload": {
    "escola": "Escola Municipal Centro",
    "responsavel": "João",
    "mensagem": "amanhã não haverá aula"
  }
}
```
**Resposta `202 Accepted`**
```json
{
  "id": "uuid",
  "status": "PENDING"
}
```

#### Consultar status
```
GET /notifications/{id}
X-API-Key: {sua-api-key}
```

---

### Admin — DLQ

#### Listar mensagens na DLQ
```
GET /admin/dlq/messages
```

#### Reprocessar mensagens da DLQ
```
POST /admin/dlq/reprocess
```

---

## Retry com Backoff Exponencial

Quando uma entrega falha, o serviço agenda reenvios automáticos com delays crescentes:

```
Tentativa 1 → falhou → aguarda 2s
Tentativa 2 → falhou → aguarda 4s
Tentativa 3 → falhou → aguarda 8s
Tentativa 4 → falhou → aguarda 16s
Tentativa 5 → falhou → aguarda 32s → DLQ
```

Mensagens que esgotam todas as tentativas vão para a Dead Letter Queue e podem ser reprocessadas manualmente via `POST /admin/dlq/reprocess` após o problema ser resolvido.

---

## Rodando localmente

**Pré-requisitos:** Docker Desktop, Java 21

```bash
# 1. Clone o repositório
git clone https://github.com/Thiago-lemes/notification-service.git
cd notification-service

# 2. Suba a infraestrutura
docker-compose up -d

# 3. Configure as variáveis de ambiente (necessário para WhatsApp)
# TWILIO_ACCOUNT_SID=seu-account-sid
# TWILIO_AUTH_TOKEN=seu-auth-token

# 4. Rode a aplicação
./mvnw spring-boot:run
```

**Serviços disponíveis:**

| Serviço | URL |
|---|---|
| API | http://localhost:8080 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| Mailhog (UI de email) | http://localhost:8025 |

---

## Testes

O projeto tem **32 testes unitários** cobrindo todos os casos de uso — sem subir Spring, banco ou broker.

```bash
./mvnw test
```

Cada caso de uso é testado com MockK — implementações falsas das interfaces permitem testar a lógica de negócio em isolamento total.

---

## Estrutura do projeto

```
src/main/kotlin/dev/thiago/notification_service/
├── domain/
│   ├── model/
│   ├── port/
│   │   ├── input/
│   │   └── output/
│   └── service/
├── application/
│   └── usecase/
└── infrastructure/
    ├── channel/
    ├── messaging/
    ├── persistence/
    └── web/
```

---

## Architectural Decision Records

Todas as decisões arquiteturais significativas estão documentadas em [`/docs/adr`](./docs/adr).

| # | Decisão | Status |
|---|---|---|
| [ADR-001](./docs/adr/001-hexagonal-architecture.md) | Arquitetura Hexagonal | Aceito |
| [ADR-002](./docs/adr/002-rabbitmq-async-delivery.md) | Entrega assíncrona com RabbitMQ | Aceito |
| [ADR-003](./docs/adr/003-strategy-pattern-channels.md) | Padrão Strategy para canais | Aceito |
| [ADR-004](./docs/adr/004-flyway-migrations.md) | Migrations com Flyway | Aceito |
| [ADR-005](./docs/adr/005-retry-backoff-exponencial.md) | Retry com Backoff Exponencial | Aceito |

---

## Licença

MIT