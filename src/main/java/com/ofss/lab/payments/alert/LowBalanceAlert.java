package com.ofss.lab.payments.alert;

import java.math.BigDecimal;

/**
 * Describes a single low-balance alert for an account whose balance is below
 * the configured threshold.
 *
 * @param accountId  the account identifier
 * @param balance    the account's current balance
 * @param threshold  the configured low-balance threshold
 * @param shortfall  the amount by which the balance falls short of the threshold
 * @param reasonCode the code explaining the alert (see {@link LowBalanceAlertService})
 */
public record LowBalanceAlert(
        String accountId,
        BigDecimal balance,
        BigDecimal threshold,
        BigDecimal shortfall,
        String reasonCode) {
}