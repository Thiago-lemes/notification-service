# ADR-001: Arquitetura Hexagonal

- **Status:** Aceito
- **Data:** 2026-04

---

## Contexto

O serviço precisa suportar múltiplos canais de entrega, múltiplos tenants, e evoluir sem que mudanças de infraestrutura afetem as regras de negócio. Precisávamos de uma arquitetura que tornasse o domínio testável de forma isolada e que facilitasse a substituição de componentes externos.

## Decisão

Adotamos a **Arquitetura Hexagonal** (Ports and Adapters), organizando o código em três camadas:

- **Domínio** — modelos e interfaces (portas), sem dependências de framework
- **Aplicação** — casos de uso que orquestram o domínio
- **Infraestrutura** — adapters que implementam as portas usando tecnologias reais (JPA, RabbitMQ, SMTP, Twilio)

## Consequências

**Positivas:**
- Casos de uso são testáveis com mocks simples, sem subir banco ou broker
- Trocar PostgreSQL, RabbitMQ ou qualquer canal de entrega afeta apenas os adapters
- As interfaces de porta documentam explicitamente as dependências do domínio
- Separação clara de responsabilidades facilita onboarding de novos desenvolvedores

**Negativas:**
- Mais arquivos e mais código boilerplate do que uma arquitetura em camadas simples
- Curva de aprendizado inicial para quem não conhece o padrão
- Mapeamento manual entre modelos de domínio e entidades JPA

## Alternativas consideradas

**Arquitetura em camadas tradicional (Controller → Service → Repository):** descartada porque acopla o domínio ao JPA e dificulta testes unitários isolados.

**Spring Modulith:** considerado para modularização futura, mas optamos pela hexagonal para ter isolamento mais explícito desde o início.
