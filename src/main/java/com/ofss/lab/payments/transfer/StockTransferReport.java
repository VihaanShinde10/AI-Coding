package com.ofss.lab.payments.transfer;

import java.math.BigDecimal;

/**
 * Immutable report describing a completed stock transfer and its fee breakdown.
 *
 * @param sourceAccount   the account the shares were transferred from
 * @param destinationAccount the account the shares were transferred to
 * @param symbol          the stock symbol transferred
 * @param quantity        the number of shares transferred
 * @param pricePerShare   the price per share used to compute the transfer value
 * @param currency        the transaction currency
 * @param customerTier    the customer tier
 * @param isInternational whether the transfer was international
 * @param baseFee         the fee without the international surcharge
 * @param surcharge       the international surcharge applied on top
 * @param fee             the total transfer fee
 */
public record StockTransferReport(
        String sourceAccount,
        String destinationAccount,
        String symbol,
        BigDecimal quantity,
        BigDecimal pricePerShare,
        String currency,
        String customerTier,
        boolean isInternational,
        BigDecimal baseFee,
        BigDecimal surcharge,
        BigDecimal fee) {
}