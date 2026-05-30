# ADR-004: Migrations com Flyway

- **Status:** Aceito
- **Data:** 2026-04

---

## Contexto

O schema do banco de dados precisa evoluir de forma controlada e reproduzível entre ambientes (local, staging, produção). Precisávamos de uma estratégia que tornasse o histórico do schema rastreável e o setup local simples.

## Decisão

Adotamos o **Flyway** para gerenciamento de migrations, com as seguintes convenções:

- Migrations versionadas sequencialmente: `V1__`, `V2__`, `V3__`...
- Cada migration tem responsabilidade única (uma tabela por migration)
- Migrations incluem dados de seed para facilitar o desenvolvimento local
- `ddl-auto: validate` no JPA — o Hibernate valida o schema mas nunca o modifica

```
src/main/resources/db/migration/
├── V1__create_tenants.sql
├── V2__create_notifications.sql
├── V3__create_recipients.sql
├── V4__create_notification_deliveries.sql
├── V5__create_recipient_groups.sql
├── V6__create_group_members.sql
└── V7__create_templates.sql
```

## Consequências

**Positivas:**
- Schema versionado junto com o código — PR de feature inclui migration correspondente
- Setup local reproduzível — `docker-compose up + mvnw spring-boot:run` cria o schema automaticamente
- Histórico completo de evolução do banco rastreável pelo Git
- `ddl-auto: validate` previne divergências silenciosas entre entidades JPA e schema real

**Negativas:**
- Migrations são imutáveis após aplicadas — erros exigem uma nova migration corretiva
- Em ambientes com múltiplas instâncias, o Flyway precisa de lock distribuído (já suportado nativamente)

## Alternativas consideradas

**`ddl-auto: update`:** descartado por ser imprevisível em produção — o Hibernate não faz rollback e pode perder dados em alterações de coluna.

**Liquibase:** considerado, mas Flyway é mais simples para o escopo atual. Liquibase seria preferível se houvesse necessidade de rollback automático de migrations.
