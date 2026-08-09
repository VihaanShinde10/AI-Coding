package com.ofss.lab.payments.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class LowBalanceAlertServiceTest {

    private final LowBalanceAlertService service = new LowBalanceAlertService();

    @Test
    void evaluatesNormalCaseWithReasonCodesAndShortfall() {
        List<AccountBalance> accounts = List.of(
                new AccountBalance("ACC-001", new BigDecimal("50.00")),
                new AccountBalance("ACC-002", new BigDecimal("150.00")),
                new AccountBalance("ACC-003", new BigDecimal("99.99")));

        List<LowBalanceAlert> alerts = service.evaluate(accounts, new BigDecimal("100.00"));

        assertEquals(2, alerts.size());

        LowBalanceAlert first = alerts.get(0);
        assertEquals("ACC-001", first.accountId());
        assertEquals(new BigDecimal("50.00"), first.shortfall());
        assertEquals(LowBalanceAlertService.REASON_BELOW_THRESHOLD, first.reasonCode());

        LowBalanceAlert second = alerts.get(1);
        assertEquals("ACC-003", second.accountId());
        assertEquals(new BigDecimal("0.01"), second.shortfall());
        assertEquals(LowBalanceAlertService.REASON_BELOW_THRESHOLD, second.reasonCode());
    }

    @Test
    void balanceExactlyAtThresholdProducesNoAlert() {
        List<AccountBalance> accounts = List.of(
                new AccountBalance("ACC-001", new BigDecimal("100.00")));

        List<LowBalanceAlert> alerts = service.evaluate(accounts, new BigDecimal("100.00"));

        assertTrue(alerts.isEmpty());
    }

    @Test
    void balanceJustBelowThresholdProducesAlert() {
        List<AccountBalance> accounts = List.of(
                new AccountBalance("ACC-001", new BigDecimal("99.99")));

        List<LowBalanceAlert> alerts = service.evaluate(accounts, new BigDecimal("100.00"));

        assertEquals(1, alerts.size());
        assertEquals(new BigDecimal("0.01"), alerts.get(0).shortfall());
        assertEquals(LowBalanceAlertService.REASON_BELOW_THRESHOLD, alerts.get(0).reasonCode());
    }

    @Test
    void emptyInputProducesNoAlerts() {
        List<LowBalanceAlert> alerts = service.evaluate(List.of(), new BigDecimal("100.00"));

        assertTrue(alerts.isEmpty());
    }

    @Test
    void allBelowThresholdProducesAlertForEveryAccount() {
        List<AccountBalance> accounts = List.of(
                new AccountBalance("ACC-001", new BigDecimal("10.00")),
                new AccountBalance("ACC-002", new BigDecimal("20.00")));

        List<LowBalanceAlert> alerts = service.evaluate(accounts, new BigDecimal("100.00"));

        assertEquals(2, alerts.size());
        assertEquals("ACC-001", alerts.get(0).accountId());
        assertEquals("ACC-002", alerts.get(1).accountId());
    }

    @Test
    void zeroAndNegativeBalancesProduceDistinctReasonCodes() {
        List<AccountBalance> accounts = List.of(
                new AccountBalance("ACC-001", BigDecimal.ZERO),
                new AccountBalance("ACC-002", new BigDecimal("-20.00")));

        List<LowBalanceAlert> alerts = service.evaluate(accounts, new BigDecimal("100.00"));

        assertEquals(2, alerts.size());
        assertEquals(LowBalanceAlertService.REASON_NEGATIVE_BALANCE, alerts.get(0).reasonCode());
        assertEquals(LowBalanceAlertService.REASON_ZERO_BALANCE, alerts.get(1).reasonCode());
        // Shortfall for zero balance: 100.00 - 0.00 = 100.00.
        assertEquals(new BigDecimal("100.00"), alerts.get(1).shortfall());
    }

    @Test
    void sortsByUrgencyThenShortfallDescending() {
        List<AccountBalance> accounts = List.of(
                new AccountBalance("ACC-BT-1", new BigDecimal("80.00")),
                new AccountBalance("ACC-ZERO", BigDecimal.ZERO),
                new AccountBalance("ACC-NEG", new BigDecimal("-5.00")),
                new AccountBalance("ACC-BT-2", new BigDecimal("50.00")));

        List<LowBalanceAlert> alerts = service.evaluate(accounts, new BigDecimal("100.00"));

        assertEquals("ACC-NEG", alerts.get(0).accountId());
        assertEquals("ACC-ZERO", alerts.get(1).accountId());
        // Below-threshold alerts sorted by shortfall descending: BT-2 shortfall 50 > BT-1 shortfall 20.
        assertEquals("ACC-BT-2", alerts.get(2).accountId());
        assertEquals("ACC-BT-1", alerts.get(3).accountId());
    }

    @Test
    void formatSummaryProducesTextSummary() {
        List<LowBalanceAlert> alerts = List.of(new LowBalanceAlert(
                "ACC-001", new BigDecimal("50.00"), new BigDecimal("100.00"),
                new BigDecimal("50.00"), LowBalanceAlertService.REASON_BELOW_THRESHOLD));

        String summary = service.formatSummary(alerts);

        assertEquals(
                "Low balance alerts: 1" + System.lineSeparator()
                        + "- ACC-001: balance 50.00, threshold 100.00, shortfall 50.00 [BELOW_THRESHOLD]",
                summary);
    }

    @Test
    void formatSummaryForEmptyAlertsShowsZero() {
        assertEquals("Low balance alerts: 0", service.formatSummary(List.of()));
    }

    @Test
    void evaluateRejectsNullThreshold() {
        assertThrows(IllegalArgumentException.class,
                () -> service.evaluate(List.of(), null));
    }

    @Test
    void evaluateRejectsNegativeThreshold() {
        assertThrows(IllegalArgumentException.class,
                () -> service.evaluate(List.of(), new BigDecimal("-1.00")));
    }

    @Test
    void evaluateRejectsNullBalance() {
        List<AccountBalance> accounts = List.of(
                new AccountBalance("ACC-001", null));

        assertThrows(IllegalArgumentException.class,
                () -> service.evaluate(accounts, new BigDecimal("100.00")));
    }
}