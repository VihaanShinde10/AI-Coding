package com.ofss.lab.payments.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ofss.lab.payments.service.FeeCalculator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentReportServiceTest {

    private final PaymentReportService reportService =
            new PaymentReportService(new FeeCalculator());

    @Test
    void buildReportCapturesTransactionFieldsAndCalculatedFee() {
        PaymentReport report = reportService.buildReport(
                new BigDecimal("5000.00"), PaymentReportService.USD, "STANDARD", false);

        assertEquals(new BigDecimal("5000.00"), report.amount());
        assertEquals(PaymentReportService.USD, report.currency());
        assertEquals("STANDARD", report.customerTier());
        assertFalse(report.isInternational());
        assertEquals(new BigDecimal("25.00"), report.baseFee());
        assertEquals(new BigDecimal("0.00"), report.surcharge());
        assertEquals(new BigDecimal("25.00"), report.fee());
    }

    @Test
    void buildReportIncludesInternationalFlagAndSurcharge() {
        PaymentReport report = reportService.buildReport(
                new BigDecimal("5000.00"), PaymentReportService.INR, "PREMIUM", true);

        assertEquals(new BigDecimal("5000.00"), report.amount());
        assertEquals(PaymentReportService.INR, report.currency());
        assertEquals("PREMIUM", report.customerTier());
        assertTrue(report.isInternational());
        // 5000.00 * 0.25% = 12.50 base + 15.00 surcharge = 27.50.
        assertEquals(new BigDecimal("12.50"), report.baseFee());
        assertEquals(new BigDecimal("15.00"), report.surcharge());
        assertEquals(new BigDecimal("27.50"), report.fee());
    }

    @Test
    void buildReportRejectsUnsupportedCurrency() {
        assertThrows(IllegalArgumentException.class,
                () -> reportService.buildReport(
                        new BigDecimal("5000.00"), "EUR", "STANDARD", false));
    }

    @Test
    void buildReportRejectsNullCurrency() {
        assertThrows(IllegalArgumentException.class,
                () -> reportService.buildReport(
                        new BigDecimal("5000.00"), null, "STANDARD", false));
    }
}