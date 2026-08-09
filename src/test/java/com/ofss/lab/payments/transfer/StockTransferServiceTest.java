package com.ofss.lab.payments.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ofss.lab.payments.service.FeeCalculator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class StockTransferServiceTest {

    private final StockTransferService service = new StockTransferService(new FeeCalculator());

    private StockTransferRequest validRequest() {
        return new StockTransferRequest(
                "ACC-1001", "ACC-2002", "AAPL",
                new BigDecimal("10"), new BigDecimal("500.00"),
                StockTransferService.USD, "STANDARD", false);
    }

    @Test
    void transferCalculatesFeeFromQuantityTimesPrice() {
        // 10 * 500.00 = 5000.00 -> 0.5% = 25.00.
        StockTransferReport report = service.transfer(validRequest());

        assertEquals("ACC-1001", report.sourceAccount());
        assertEquals("ACC-2002", report.destinationAccount());
        assertEquals("AAPL", report.symbol());
        assertEquals(new BigDecimal("10"), report.quantity());
        assertEquals(new BigDecimal("500.00"), report.pricePerShare());
        assertEquals(StockTransferService.USD, report.currency());
        assertEquals("STANDARD", report.customerTier());
        assertEquals(new BigDecimal("25.00"), report.baseFee());
        assertEquals(new BigDecimal("0.00"), report.surcharge());
        assertEquals(new BigDecimal("25.00"), report.fee());
    }

    @Test
    void transferAppliesInternationalSurcharge() {
        StockTransferRequest request = new StockTransferRequest(
                "ACC-1001", "ACC-2002", "AAPL",
                new BigDecimal("10"), new BigDecimal("500.00"),
                StockTransferService.INR, "PREMIUM", true);

        StockTransferReport report = service.transfer(request);

        // 10 * 500.00 = 5000.00 -> 0.25% = 12.50 + 15.00 surcharge = 27.50.
        assertEquals(new BigDecimal("12.50"), report.baseFee());
        assertEquals(new BigDecimal("15.00"), report.surcharge());
        assertEquals(new BigDecimal("27.50"), report.fee());
    }

    @Test
    void transferRejectsNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.transfer(null));
    }

    @Test
    void transferRejectsBlankAccounts() {
        StockTransferRequest request = new StockTransferRequest(
                "", "ACC-2002", "AAPL",
                new BigDecimal("10"), new BigDecimal("500.00"),
                StockTransferService.USD, "STANDARD", false);

        assertThrows(IllegalArgumentException.class, () -> service.transfer(request));
    }

    @Test
    void transferRejectsBlankSymbol() {
        StockTransferRequest request = new StockTransferRequest(
                "ACC-1001", "ACC-2002", " ",
                new BigDecimal("10"), new BigDecimal("500.00"),
                StockTransferService.USD, "STANDARD", false);

        assertThrows(IllegalArgumentException.class, () -> service.transfer(request));
    }

    @Test
    void transferRejectsNonPositiveQuantity() {
        StockTransferRequest request = new StockTransferRequest(
                "ACC-1001", "ACC-2002", "AAPL",
                BigDecimal.ZERO, new BigDecimal("500.00"),
                StockTransferService.USD, "STANDARD", false);

        assertThrows(IllegalArgumentException.class, () -> service.transfer(request));
    }

    @Test
    void transferRejectsNonPositivePricePerShare() {
        StockTransferRequest request = new StockTransferRequest(
                "ACC-1001", "ACC-2002", "AAPL",
                new BigDecimal("10"), new BigDecimal("-1.00"),
                StockTransferService.USD, "STANDARD", false);

        assertThrows(IllegalArgumentException.class, () -> service.transfer(request));
    }

    @Test
    void transferRejectsUnsupportedCurrency() {
        StockTransferRequest request = new StockTransferRequest(
                "ACC-1001", "ACC-2002", "AAPL",
                new BigDecimal("10"), new BigDecimal("500.00"),
                "EUR", "STANDARD", false);

        assertThrows(IllegalArgumentException.class, () -> service.transfer(request));
    }
}