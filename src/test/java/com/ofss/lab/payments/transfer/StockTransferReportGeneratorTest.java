package com.ofss.lab.payments.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class StockTransferReportGeneratorTest {

    private final StockTransferReportGenerator generator = new StockTransferReportGenerator();

    @Test
    void headerListsAllColumns() {
        assertEquals(
                "sourceAccount,destinationAccount,symbol,quantity,pricePerShare,currency,customerTier,isInternational,baseFee,surcharge,fee",
                generator.header());
    }

    @Test
    void toCsvFormatsReportAsSingleLine() {
        StockTransferReport report = new StockTransferReport(
                "ACC-1001", "ACC-2002", "AAPL",
                new BigDecimal("10"), new BigDecimal("500.00"),
                StockTransferService.USD, "STANDARD", false,
                new BigDecimal("25.00"), new BigDecimal("0.00"), new BigDecimal("25.00"));

        assertEquals(
                "ACC-1001,ACC-2002,AAPL,10,500.00,USD,STANDARD,false,25.00,0.00,25.00",
                generator.toCsv(report));
    }

    @Test
    void toCsvIncludesInternationalFlagAndSurcharge() {
        StockTransferReport report = new StockTransferReport(
                "ACC-1001", "ACC-2002", "AAPL",
                new BigDecimal("10"), new BigDecimal("500.00"),
                StockTransferService.INR, "PREMIUM", true,
                new BigDecimal("12.50"), new BigDecimal("15.00"), new BigDecimal("27.50"));

        assertEquals(
                "ACC-1001,ACC-2002,AAPL,10,500.00,INR,PREMIUM,true,12.50,15.00,27.50",
                generator.toCsv(report));
    }
}