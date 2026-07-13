package com.aif.mfg.paymentengine.traceability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TraceabilityTest {

    @Test
    @DisplayName("should keep source and Jira traceability constants aligned")
    void shouldKeepSourceAndJiraTraceabilityConstantsAligned() {
        assertEquals("MFG-ARC-PAY-CORE-001", Traceability.SOURCE_DOC_ID);
        assertEquals("v1.0", Traceability.SOURCE_VERSION);
        assertEquals("AIF-179", Traceability.JIRA_TRACKING_ISSUE);
        assertTrue(Traceability.PE001_SLICE.contains("EPIC-001"));
        assertTrue(Traceability.PE001_SLICE.contains("US-001"));
        assertTrue(Traceability.PE001_SLICE.contains("US-003"));
        assertTrue(Traceability.PE001_SLICE.contains("US-004"));
        assertTrue(Traceability.PE001_SLICE.contains("AC-001"));
        assertTrue(Traceability.PE001_SLICE.contains("AC-012"));
    }
}
