package com.ofss.lab.payments.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportGeneratorTest {

    private final ReportGenerator generator = new ReportGenerator();

    @Test
    void headerListsAllColumns() {
        assertEquals("currency,amount,customerTier,isInternational,baseFee,surcharge,fee",
                generator.header());
    }

    @Test
    void toCsvFormatsReportAsSingleLine() {
        PaymentReport report = new PaymentReport(
                new BigDecimal("5000.00"), PaymentReportService.USD, "STANDARD", false,
                new BigDecimal("25.00"), new BigDecimal("0.00"), new BigDecimal("25.00"));

        assertEquals("USD,5000.00,STANDARD,false,25.00,0.00,25.00", generator.toCsv(report));
    }

    @Test
    void toCsvIncludesInternationalFlagAndSurcharge() {
        PaymentReport report = new PaymentReport(
                new BigDecimal("500.00"), PaymentReportService.INR, "PREMIUM", true,
                new BigDecimal("2.00"), new BigDecimal("15.00"), new BigDecimal("17.00"));

        assertEquals("INR,500.00,PREMIUM,true,2.00,15.00,17.00", generator.toCsv(report));
    }

    @Test
    void toCsvBatchEmitsHeaderAndOneLinePerReport() {
        PaymentReport first = new PaymentReport(
                new BigDecimal("5000.00"), PaymentReportService.USD, "STANDARD", false,
                new BigDecimal("25.00"), new BigDecimal("0.00"), new BigDecimal("25.00"));
        PaymentReport second = new PaymentReport(
                new BigDecimal("500.00"), PaymentReportService.INR, "PREMIUM", true,
                new BigDecimal("2.00"), new BigDecimal("15.00"), new BigDecimal("17.00"));

        String csv = generator.toCsvBatch(List.of(first, second));

        String expected = String.join(System.lineSeparator(),
                "currency,amount,customerTier,isInternational,baseFee,surcharge,fee",
                "USD,5000.00,STANDARD,false,25.00,0.00,25.00",
                "INR,500.00,PREMIUM,true,2.00,15.00,17.00");

        assertEquals(expected, csv);
    }
}