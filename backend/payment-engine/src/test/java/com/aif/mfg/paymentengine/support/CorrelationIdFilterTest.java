package com.aif.mfg.paymentengine.support;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    @DisplayName("should propagate provided correlation id and set response header")
    void shouldPropagateProvidedCorrelationIdAndSetResponseHeader() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn("corr-123");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-123");
        verify(filterChain).doFilter(request, response);
        assertNull(MDC.get("correlationId"));
    }

    @Test
    @DisplayName("should generate correlation id when missing")
    void shouldGenerateCorrelationIdWhenMissing() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq(CorrelationIdFilter.CORRELATION_ID_HEADER), anyString());
        verify(filterChain).doFilter(request, response);
        assertNull(MDC.get("correlationId"));
    }
}
