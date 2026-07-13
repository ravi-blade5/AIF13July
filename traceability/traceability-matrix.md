# Traceability Matrix — MFG Payment Engine Automation

## Metadata

| Field | Value |
|---|---|
| Source Document | MFG-ARC-PAY-CORE-001 |
| Source Version | v1.0 |
| Jira Tracking Issue | AIF-179 |
| Repository | ravi-blade5/AIF13July |
| Base Branch | main |
| Feature Branch | feature/payment-engine-automation |
| Commit SHA | f31be5139ce87f090dd138daafd4ad9c8ab2ba57 |
| Pull Request | https://github.com/ravi-blade5/AIF13July/pull/2 |

## Notes

- AIF-179 is the only confirmed Jira issue key.
- EPIC-001..EPIC-006 and US-001..US-029 are local SDLC workflow references.
- AC, UT, QA, MT, and API IDs are local acceptance/test artifact identifiers, not Jira test case keys.
- Implementation status values: Implemented, Partial implementation, Placeholder only, Designed only.

## Functional Requirement Traceability

| Requirement | Local Epic Ref | Local Story Ref | Jira Task | Local AC/Test IDs | Status | Design Ref | Code/Test Mapping | Commit | PR |
|---|---|---|---|---|---|---|---|---|---|
| BR-MFG-001 | EPIC-001 | US-001, US-003 | AIF-179 | AC-001, AC-002, AC-003, AC-007, AC-008, AC-009; UT-001, UT-005, UT-009, UT-013; QA-001, QA-005, QA-006, QA-012; MT-001, MT-005, MT-006; API-001, API-005, API-006 | Implemented | Step 6 §6.1 | PE001 API/DTO/domain/service/schema/tests | f31be5139ce87f090dd138daafd4ad9c8ab2ba57 | https://github.com/ravi-blade5/AIF13July/pull/2 |
| BR-MFG-002 | EPIC-002 | US-005 | AIF-179 | AC-013, AC-014, AC-015 | Placeholder only | Step 6 §6.3 | PaymentValidationPort, NoOpPaymentValidationAdapter, service handoff tests | f31be5139ce87f090dd138daafd4ad9c8ab2ba57 | https://github.com/ravi-blade5/AIF13July/pull/2 |
| BR-MFG-003 | EPIC-002 | US-006 | AIF-179 | AC-016, AC-017, AC-018 | Designed only | Step 6 §6.4 | No committed implementation in this PR |  |  |
| BR-MFG-004 | EPIC-002 | US-007 | AIF-179 | AC-019, AC-020, AC-021 | Designed only | Step 6 §6.4 | No committed implementation in this PR |  |  |
| BR-MFG-005 | EPIC-002 | US-008 | AIF-179 | AC-022, AC-023, AC-024, AC-025 | Placeholder only | Step 6 §6.5 | PaymentOrchestrationPort, NoOpPaymentOrchestrationAdapter, service handoff tests | f31be5139ce87f090dd138daafd4ad9c8ab2ba57 | https://github.com/ravi-blade5/AIF13July/pull/2 |
| BR-MFG-006 | EPIC-002 | US-008 | AIF-179 | AC-022..AC-025 | Designed only | Step 6 §6.6 | No committed implementation in this PR |  |  |
| BR-MFG-007 | EPIC-002 | US-008 | AIF-179 | Design-only coverage | Designed only | Step 6 §5.1, §6.6 | No committed implementation in this PR |  |  |
| BR-MFG-008 | EPIC-002 | US-008 | AIF-179 | AC-022..AC-025 | Designed only | Step 6 §6.6 | No committed implementation in this PR |  |  |
| BR-MFG-009 | EPIC-002 | US-009 | AIF-179 | AC-026, AC-027 | Designed only | Step 6 §6.7 | No committed implementation in this PR |  |  |
| BR-MFG-010 | EPIC-002 | US-009 | AIF-179 | AC-028 | Designed only | Step 6 §6.7 | No committed implementation in this PR |  |  |
| BR-MFG-011 | EPIC-002, EPIC-006 | US-010 | AIF-179 | AC-029, AC-030 | Designed only | Step 6 §6.8 | No committed implementation in this PR |  |  |
| BR-MFG-012 | EPIC-002, EPIC-006 | US-010, US-028 | AIF-179 | AC-031, AC-083, AC-084, AC-085 | Designed only | Step 6 §6.8, §6.12 | No committed implementation in this PR |  |  |
| BR-MFG-013 | EPIC-001, EPIC-003 | US-004, US-011 | AIF-179 | AC-010, AC-011, AC-012; UT-014, UT-015, UT-016; QA-007, QA-008, QA-009, QA-024; MT-007, MT-008, MT-009, MT-019; API-007, API-008, API-009, API-012 | Implemented for PE-001 API boundary | Step 6 §6.1, §6.2, §6.9 | Controller, ProblemDetail handler, correlation filter, related tests | f31be5139ce87f090dd138daafd4ad9c8ab2ba57 | https://github.com/ravi-blade5/AIF13July/pull/2 |
| BR-MFG-014 | EPIC-001, EPIC-003 | US-002, US-012 | AIF-179 | AC-004..AC-006, AC-035..AC-037 | Designed only | Step 6 §6.2 | No committed implementation in this PR |  |  |
| BR-MFG-015 | EPIC-003 | US-013 | AIF-179 | AC-038..AC-040 | Designed only | Step 6 §6.9 | No committed implementation in this PR |  |  |
| BR-MFG-016 | EPIC-004 | US-020 | AIF-179 | AC-059..AC-061 | Designed only | Step 6 §6.9, §6.10 | No committed implementation in this PR |  |  |
| BR-MFG-017 | EPIC-003 | US-014 | AIF-179 | AC-041..AC-043 | Designed only | Step 6 §6.9 | No committed implementation in this PR |  |  |
| BR-MFG-018 | EPIC-003 | US-015 | AIF-179 | AC-044..AC-046 | Designed only | Step 6 §6.9 | No committed implementation in this PR |  |  |
| BR-MFG-019 | EPIC-003 | US-015 | AIF-179 | AC-044, AC-045 | Designed only | Step 6 §6.9 | No committed implementation in this PR |  |  |
| BR-MFG-020 | EPIC-003 | US-015 | AIF-179 | Design-only coverage | Designed only | Step 6 §6.9 | No committed implementation in this PR |  |  |
| BR-MFG-021 | EPIC-003, EPIC-006 | US-015, US-027 | AIF-179 | AC-046, AC-080..AC-082 | Designed only | Step 6 §6.9, §6.12 | No committed implementation in this PR |  |  |
| BR-MFG-022 | EPIC-003 | US-015 | AIF-179 | AC-044, AC-045 | Designed only | Step 6 §6.9 | No committed implementation in this PR |  |  |
| BR-MFG-023 | EPIC-004 | US-016, US-019 | AIF-179 | AC-047..AC-049, AC-056..AC-058 | Designed only | Step 6 §6.10 | No committed implementation in this PR |  |  |
| BR-MFG-024 | EPIC-004 | US-017, US-020 | AIF-179 | AC-050..AC-052, AC-059..AC-061 | Designed only | Step 6 §6.10 | No committed implementation in this PR |  |  |
| BR-MFG-025 | EPIC-004 | US-016, US-018 | AIF-179 | AC-047..AC-049, AC-053..AC-055 | Designed only | Step 6 §6.10 | No committed implementation in this PR |  |  |
| BR-MFG-026 | EPIC-005 | US-021, US-022, US-023 | AIF-179 | AC-062..AC-070 | Designed only | Step 6 §6.11 | No committed implementation in this PR |  |  |
| BR-MFG-027 | EPIC-005, EPIC-006 | US-024, US-027 | AIF-179 | AC-071..AC-073, AC-080..AC-082 | Designed only | Step 6 §6.11, §6.12 | No committed implementation in this PR |  |  |
| BR-MFG-028 | EPIC-005 | US-025 | AIF-179 | AC-074..AC-076 | Designed only | Step 6 §6.11 | No committed implementation in this PR |  |  |
| BR-MFG-029 | EPIC-006 | US-026, US-029 | AIF-179 | AC-077..AC-079, AC-086, AC-087; UT-016 | Partial implementation | Step 6 §6.12 | CorrelationIdFilter and logging config only | f31be5139ce87f090dd138daafd4ad9c8ab2ba57 | https://github.com/ravi-blade5/AIF13July/pull/2 |
| BR-MFG-030 | EPIC-006 | US-027, US-028, US-029 | AIF-179 | AC-080..AC-087; UT-018 | Partial implementation | Step 6 §6.12 | Traceability constants and regression test only | f31be5139ce87f090dd138daafd4ad9c8ab2ba57 | https://github.com/ravi-blade5/AIF13July/pull/2 |

## NFR Traceability

| Requirement | Local Epic Ref | Local Story Ref | Jira Task | Local AC/Test IDs | Status | Design Ref | Code/Test Mapping | Commit | PR |
|---|---|---|---|---|---|---|---|---|---|
| NFR-MFG-001 | EPIC-002 | US-008 | AIF-179 | AC-021..AC-025 | Designed only | Step 6 §5.1, §7, §8 | No committed implementation in this PR |  |  |
| NFR-MFG-002 | EPIC-002, EPIC-006 | US-008, US-026 | AIF-179 | Design-only coverage | Designed only | Step 6 §7, §8 | No committed implementation in this PR |  |  |
| NFR-MFG-003 | EPIC-006 | US-026 | AIF-179 | Design-only coverage | Designed only | Step 6 §8 | No committed implementation in this PR |  |  |
| NFR-MFG-004 | EPIC-002 | US-007 | AIF-179 | AC-019..AC-021 | Designed only | Step 6 §6.4 | No committed implementation in this PR |  |  |
| NFR-MFG-005 | EPIC-001 | US-003, US-004 | AIF-179 | AC-007..AC-012; UT/QA/MT/API idempotency tests | Implemented | Step 6 §6.1 | Idempotency entities/repository/cleanup/key validator/service/schema/tests | f31be5139ce87f090dd138daafd4ad9c8ab2ba57 | https://github.com/ravi-blade5/AIF13July/pull/2 |
| NFR-MFG-006 | EPIC-003, EPIC-006 | US-014, US-029 | AIF-179 | AC-041..AC-043, AC-086, AC-087 | Designed only | Step 6 §6.9, §6.12 | No committed implementation in this PR |  |  |
| NFR-MFG-007 | EPIC-003, EPIC-005 | US-013, US-023 | AIF-179 | AC-038..AC-040, AC-068..AC-070 | Designed only | Step 6 §6.9, §6.11 | No committed implementation in this PR |  |  |
| NFR-MFG-008 | EPIC-005, EPIC-006 | US-024, US-027 | AIF-179 | AC-071..AC-073, AC-080..AC-082 | Designed only | Step 6 §6.11, §6.12 | No committed implementation in this PR |  |  |
| NFR-MFG-009 | EPIC-006 | US-026, US-029 | AIF-179 | AC-077..AC-079, AC-086, AC-087; UT-016, UT-018 | Partial implementation | Step 6 §6.12 | Correlation ID propagation and traceability regression only | f31be5139ce87f090dd138daafd4ad9c8ab2ba57 | https://github.com/ravi-blade5/AIF13July/pull/2 |
| NFR-MFG-010 | EPIC-006 | US-028 | AIF-179 | AC-083..AC-085 | Designed only | Step 6 §6.8, §6.12 | No committed implementation in this PR |  |  |
| NFR-MFG-011 | EPIC-001 | Channel stories | AIF-179 | Design-only coverage | Designed only | Step 6 §8 | No frontend accessibility implementation committed |  |  |
| NFR-MFG-012 | EPIC-004, EPIC-005 | US-019, US-025 | AIF-179 | AC-056..AC-058, AC-076 | Partial implementation | Step 6 §7, §14 | Maven/test setup only; no full CI/CD security scanning | f31be5139ce87f090dd138daafd4ad9c8ab2ba57 | https://github.com/ravi-blade5/AIF13July/pull/2 |

## GitHub File Groups

### Production Code

- PE001_BUILD_CONFIG: backend/payment-engine/pom.xml, backend/payment-engine/src/main/resources/application.yml, backend/payment-engine/README.md
- PE001_APP_BOOTSTRAP: backend/payment-engine/src/main/java/com/aif/mfg/paymentengine/PaymentEngineApplication.java
- PE001_API: backend/payment-engine/src/main/java/com/aif/mfg/paymentengine/api/PaymentIngressController.java
- PE001_DTO: backend/payment-engine/src/main/java/com/aif/mfg/paymentengine/api/dto/MoneyDto.java, PaymentInitiationRequest.java, PaymentAcknowledgementResponse.java
- PE001_DOMAIN: PaymentRail.java, PaymentStatus.java, SourceChannel.java, PaymentInstruction.java, PaymentInstructionRepository.java
- PE001_IDEMPOTENCY: IdempotencyStatus.java, IdempotencyRecord.java, IdempotencyRepository.java, IdempotencyCleanupJob.java
- PE001_SERVICE: IdempotencyKeyValidator.java, SourceChannelPolicy.java, PaymentInstructionMapper.java, PaymentIngressService.java
- PE001_CONFIG_SUPPORT: JacksonConfig.java, CorrelationIdFilter.java
- PE001_ERROR_HANDLING: PaymentIngressException.java, ApiExceptionHandler.java
- PE001_TRACEABILITY: Traceability.java
- PE001_DB_SCHEMA: backend/payment-engine/src/main/resources/db/migration/V1__payment_ingress_schema.sql
- PE002_PLACEHOLDER: PaymentValidationPort.java, NoOpPaymentValidationAdapter.java
- PE005_PLACEHOLDER: PaymentOrchestrationPort.java, NoOpPaymentOrchestrationAdapter.java

### Test Files

- TEST_CONFIG: TestClockConfig.java, application-test.yml, test Flyway migration
- TEST_API: PaymentIngressControllerIT.java
- TEST_SERVICE: PaymentIngressServiceTest.java
- TEST_IDEMPOTENCY: IdempotencyKeyValidatorTest.java, IdempotencyRepositoryIT.java
- TEST_POLICY: SourceChannelPolicyTest.java
- TEST_ERROR: ApiExceptionHandlerTest.java
- TEST_CORRELATION: CorrelationIdFilterTest.java
- TEST_TRACEABILITY: TraceabilityTest.java
