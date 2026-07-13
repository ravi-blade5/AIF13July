# MFG Payment Engine

Java 21 + Spring Boot implementation for the Meridian Financial Group Payment Engine.

## Traceability

| Item | Value |
|---|---|
| Source document | `MFG-ARC-PAY-CORE-001 v1.0` |
| Source file | `1783915119965/MFG-Architecture-6page.docx` |
| Scope | Payment Engine only |
| Jira epics | `AIF-161` to `AIF-164` |
| Jira stories | `AIF-165` to `AIF-168` |
| Jira technical tasks | `AIF-169` to `AIF-174` |
| Requirements | `BR-PE-001` to `BR-PE-012` |
| Acceptance criteria | `AC-001` to `AC-025` |
| Components | `PE-001` to `PE-012` |
| Flows | `F-001`, `F-002`, `F-003` |

## Implemented Capabilities

- `PE-001`: Payment ingress, source allowlist, idempotency, correlation IDs
- `PE-002`: Validation engine port and default validation adapter
- `PE-003`: Sanctions / PEP screening port and default adapter
- `PE-004`: Fraud scoring port and default adapter
- `PE-005`: Payment orchestration state machine
- `PE-006`: ISO 20022 message preparation abstraction
- `PE-007`: Liquidity decision abstraction
- `PE-008`: Settlement adapter abstraction
- `PE-009`: SDD mandate workflow
- `PE-010`: R-transaction handling
- `PE-011`: Reconciliation
- `PE-012`: Reporting aggregation

## Local Run

```bash
mvn clean verify
mvn spring-boot:run
```

OpenAPI UI: `http://localhost:8080/swagger-ui.html`

Health and metrics:

```text
http://localhost:8080/actuator/health
http://localhost:8080/actuator/prometheus
```

## Example Instant Payment

```bash
curl -X POST http://localhost:8080/v1/payments/instant \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-key-001' \
  -H 'X-Source-Channel: MOBILE' \
  -d '{"debtorIban":"DE89370400440532013000","creditorIban":"FR1420041010050500013M02606","creditorName":"Example Merchant","amount":25.50,"currency":"EUR","remittanceInformation":"Invoice 123"}'
```

## Production Adapter Notes

Local default adapters are provided for development and tests. Production deployments must replace ports for Redis Enterprise (`DB-005`), Kafka/Event Backbone (`INT-005`), sanctions (`SEC-008`), fraud (`SEC-009`), ledger facade (`CS-003`), MQ/CSM/RTGS settlement, PKI/HSM, and SPIFFE/SPIRE workload identity.
