package com.ofss.lab.payments.alert;

import java.math.BigDecimal;

/**
 * Describes a single account's current balance.
 *
 * @param accountId the account identifier
 * @param balance   the account's current balance (must not be null)
 */
public record AccountBalance(
        String accountId,
        BigDecimal balance) {
}