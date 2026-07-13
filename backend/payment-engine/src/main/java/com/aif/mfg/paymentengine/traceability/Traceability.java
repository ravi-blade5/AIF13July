package com.aif.mfg.paymentengine.traceability;

import java.util.List;

public final class Traceability {

    public static final String SOURCE_DOC_ID = "MFG-ARC-PAY-CORE-001";
    public static final String SOURCE_VERSION = "v1.0";
    public static final String JIRA_TRACKING_ISSUE = "AIF-179";

    public static final String PE001_SLICE = "MFG-ARC-PAY-CORE-001:v1.0|AIF-179|EPIC-001|US-001,US-003,US-004|AC-001..AC-012|PE-001,DB-005";
    public static final String PE002_PLACEHOLDER = "MFG-ARC-PAY-CORE-001:v1.0|AIF-179|EPIC-002|US-005|AC-013..AC-015|PE-002";
    public static final String PE005_PLACEHOLDER = "MFG-ARC-PAY-CORE-001:v1.0|AIF-179|EPIC-002|US-008|AC-022..AC-025|PE-005";

    public static final List<String> COMPONENT_IDS = List.of("PE-001", "PE-002", "PE-005", "DB-005", "INT-001", "INT-002");
    public static final List<String> STORY_IDS = List.of("US-001", "US-003", "US-004");
    public static final List<String> ACCEPTANCE_CRITERIA_IDS = List.of("AC-001", "AC-002", "AC-003", "AC-007", "AC-008", "AC-009", "AC-010", "AC-011", "AC-012");

    private Traceability() {
    }
}
