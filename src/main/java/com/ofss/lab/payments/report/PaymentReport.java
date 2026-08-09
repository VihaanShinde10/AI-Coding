package com.ofss.lab.payments.report;

import java.math.BigDecimal;

/**
 * Immutable data holder describing a single payment transaction and the fee
 * calculated for it by the payment service.
 *
 * <p>The fee is broken down into the base fee (calculated without the
 * international surcharge) and the surcharge applied on top.</p>
 */
public record PaymentReport(
        BigDecimal amount,
        String currency,
        String customerTier,
        boolean isInternational,
        BigDecimal baseFee,
        BigDecimal surcharge,
        BigDecimal fee) {
}