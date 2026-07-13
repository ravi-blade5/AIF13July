# Payment Engine Consolidated Traceability Matrix

## Summary

| Field | Value |
|---|---|
| Source Document | `MFG-ARC-PAY-CORE-001 v1.0` |
| Source File | `1783915119965/MFG-Architecture-6page.docx` / uploaded `MFG-Architecture-6page.docx` |
| Scope | Payment Engine only |
| Jira Project | `AIF` |
| Jira Epics | `AIF-161` to `AIF-164` |
| Jira Stories | `AIF-165` to `AIF-168` |
| Jira Technical Tasks | `AIF-169` to `AIF-174` |
| Local QA Test Cases | `TC-PE-001` to `TC-PE-010`; no Jira test case issue keys created/provided |
| GitHub Repository | `https://github.com/ravi-blade5/AIF13July` |
| GitHub Branch | `payment-engine-sdlc-automation` |
| Prior Traceability Commit | `0ee4a657140ce5bf35862338a3de1140e17ee1ac` |
| Implementation and QA Commit | `40e67c6fbe1251495e387105e276459c8ff686ef` |
| Pull Request | Not created |

## Source Section Coverage

| Source Section | Coverage |
|---|---|
| Section 1 | Architecture principles, API-first, event-driven, zero-trust, resilience |
| Section 2 | Trust boundaries, TPP, CSM, RTGS, regulator, service-to-service security |
| Section 3 | L3 Payment Engine logical architecture |
| Section 4 | Payment Engine transaction spine, rails, ISO 20022 message families |
| Section 4.1 | `PE-001` through `PE-012` component inventory |
| Section 4.2 | SCT Inst critical path and p99 `<10s` constraints |
| Section 5 | Integration Fabric, idempotency, RFC 7807, event backbone, MFT, MQ |
| Section 6 | Core dependencies including `CS-003`, `CS-017`, `CS-018` |
| Section 8 | Data architecture including `DB-005`, `DB-008`, `DB-009`, `DB-010` |
| Section 9 | Security dependencies including `SEC-003`, `SEC-008`, `SEC-009` |
| Section 10 | NFR tiers, latency, availability, RPO/RTO, auditability |
| Section 11 | CI/CD, observability, resilience, SBOM, SAST/SCA/DAST |
| Section 12 | Reference flows `F-001`, `F-002`, `F-003` |
| Section 13 | SDLC decomposition, traceability, regulatory refs, DoD |

## Jira Hierarchy

| Local Ref | Jira Key | Type | Summary |
|---|---|---|---|
| EPIC-001 | AIF-161 | Epic | Instant Payment Ingress, Validation, Screening, and Orchestration |
| EPIC-002 | AIF-162 | Epic | Direct Debit Mandate and Batch Collection Processing |
| EPIC-003 | AIF-163 | Epic | Settlement, Liquidity, and Clearing Connectivity |
| EPIC-004 | AIF-164 | Epic | Payment Exceptions, Reconciliation, and Reporting |
| US-PE-001 | AIF-165 | Story | Accept, validate, screen, and orchestrate instant payment instructions |
| US-PE-002 | AIF-166 | Story | Accept and process direct debit batch instructions with mandate validation |
| US-PE-003 | AIF-167 | Story | Check liquidity and submit payments to the correct settlement endpoint |
| US-PE-004 | AIF-168 | Story | Handle exceptions, reconcile outcomes, and produce payment reporting |
| TT-PE-001 | AIF-169 | Task | Implement payment ingress idempotency and correlation handling |
| TT-PE-002 | AIF-170 | Task | Implement validation, sanctions, and fraud decisioning integration |
| TT-PE-003 | AIF-171 | Task | Implement payment orchestration state machine and ISO 20022 message preparation |
| TT-PE-004 | AIF-172 | Task | Implement SDD batch fan-out and mandate validation workflow |
| TT-PE-005 | AIF-173 | Task | Implement liquidity checks and settlement adapter integration |
| TT-PE-006 | AIF-174 | Task | Implement R-transaction handling, reconciliation, and reporting pipeline |

## Requirement-to-Delivery Matrix

| Requirement | Components | Flow(s) | Jira Epic(s) | Jira Story/Stories | Technical Task(s) | AC IDs | Local Test Cases | GitHub Evidence |
|---|---|---|---|---|---|---|---|---|
| BR-PE-001 | PE-001 | F-001, F-002, F-003 | AIF-161, AIF-162 | AIF-165, AIF-166 | AIF-169, AIF-172 | AC-001, AC-005, AC-006, AC-008, AC-011 | TC-PE-001, TC-PE-003, TC-PE-004, TC-PE-005, TC-PE-006 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-002 | PE-002 | F-001, F-002, F-003 | AIF-161, AIF-162 | AIF-165, AIF-166 | AIF-170, AIF-172 | AC-002, AC-009, AC-012 | TC-PE-002, TC-PE-005 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-003 | PE-003 | F-001, F-002, F-003 | AIF-161, AIF-162 | AIF-165, AIF-166 | AIF-170, AIF-172 | AC-003, AC-010 | TC-PE-001, TC-PE-002, TC-PE-005 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-004 | PE-004 | F-001, F-003 | AIF-161 | AIF-165 | AIF-170 | AC-003, AC-007 | TC-PE-001, TC-PE-002 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-005 | PE-005 | F-001, F-002, F-003 | AIF-161, AIF-162, AIF-163 | AIF-165, AIF-166, AIF-167 | AIF-171, AIF-172, AIF-173 | AC-004, AC-007, AC-010, AC-017 | TC-PE-001, TC-PE-005, TC-PE-007, TC-PE-009 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-006 | PE-006 | F-001, F-002, F-003 | AIF-161, AIF-162, AIF-163 | AIF-165, AIF-166, AIF-167 | AIF-171, AIF-172, AIF-173 | AC-004, AC-010, AC-016 | TC-PE-001, TC-PE-005, TC-PE-007 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-007 | PE-007 | F-001, F-002 | AIF-163 | AIF-167 | AIF-173 | AC-014, AC-015, AC-019 | TC-PE-007, TC-PE-008 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-008 | PE-008 | F-001, F-002, F-003 | AIF-163 | AIF-167 | AIF-173 | AC-016, AC-017, AC-018 | TC-PE-007, TC-PE-008 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-009 | PE-009 | F-002 | AIF-162 | AIF-166 | AIF-172 | AC-008, AC-009, AC-010, AC-011, AC-012, AC-013 | TC-PE-005, TC-PE-006 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-010 | PE-010 | F-001, F-002, F-003 | AIF-164 | AIF-168 | AIF-174 | AC-020, AC-021 | TC-PE-009 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-011 | PE-011 | F-001, F-002 | AIF-164 | AIF-168 | AIF-174 | AC-022, AC-023 | TC-PE-010 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |
| BR-PE-012 | PE-012 | F-001, F-002, F-003 | AIF-164 | AIF-168 | AIF-174 | AC-024, AC-025 | TC-PE-010 | Commit `40e67c6fbe1251495e387105e276459c8ff686ef` |

## GitHub Files Covered

### Implementation Files

- `pom.xml`
- `.gitignore`
- `Dockerfile`
- `README.md`
- `.github/workflows/ci.yml`
- `src/main/resources/application.yml`
- `src/main/java/com/mfg/paymentengine/PaymentEngineApplication.java`
- `src/main/java/com/mfg/paymentengine/config/PaymentEngineProperties.java`
- `src/main/java/com/mfg/paymentengine/config/SecurityConfig.java`
- `src/main/java/com/mfg/paymentengine/api/dto/PaymentDtos.java`
- `src/main/java/com/mfg/paymentengine/domain/PaymentModels.java`
- `src/main/java/com/mfg/paymentengine/service/Ports.java`
- `src/main/java/com/mfg/paymentengine/service/PaymentWorkflowService.java`
- `src/main/java/com/mfg/paymentengine/api/PaymentController.java`
- `src/main/java/com/mfg/paymentengine/infra/InMemoryAdapters.java`
- `src/main/java/com/mfg/paymentengine/infra/DefaultComponentAdapters.java`
- `src/main/java/com/mfg/paymentengine/error/ApiExceptionHandler.java`

### Test and QA Files

- `src/test/java/com/mfg/paymentengine/service/PaymentWorkflowServiceTest.java`
- `src/test/java/com/mfg/paymentengine/integration/PaymentWorkflowIntegrationTest.java`
- `src/test/java/com/mfg/paymentengine/api/PaymentControllerApiTest.java`
- `src/test/java/com/mfg/paymentengine/api/PaymentControllerAutomationTest.java`
- `src/test/java/com/mfg/paymentengine/service/PaymentWorkflowAutomationTest.java`
- `test-data/payment-engine-test-data.json`
- `test-data/payment-engine-automation-data.json`
- `scripts/run_payment_engine_tests.sh`
- `scripts/run_payment_engine_automation_tests.sh`

### Existing Traceability Files

- `docs/traceability/payment-engine-traceability.json` — prior traceability commit `0ee4a657140ce5bf35862338a3de1140e17ee1ac`
- `docs/traceability/payment-engine-traceability.md` — prior traceability commit `0ee4a657140ce5bf35862338a3de1140e17ee1ac`

## Pull Request Status

No pull request has been created for this workflow. Current GitHub evidence is commit-based on branch `payment-engine-sdlc-automation`.

## Prepared By

Traceability Sync Agent
