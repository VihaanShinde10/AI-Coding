package com.ofss.lab.payments.transfer;

import java.math.BigDecimal;

/**
 * Immutable request describing a stock transfer between two accounts.
 *
 * @param sourceAccount   the account the shares are transferred from
 * @param destinationAccount the account the shares are transferred to
 * @param symbol          the stock symbol being transferred
 * @param quantity        the number of shares being transferred
 * @param pricePerShare   the price per share used to compute the transfer value
 * @param currency        the transaction currency (must be USD or INR)
 * @param customerTier    the customer tier (e.g. STANDARD or PREMIUM)
 * @param isInternational whether the transfer is international
 */
public record StockTransferRequest(
        String sourceAccount,
        String destinationAccount,
        String symbol,
        BigDecimal quantity,
        BigDecimal pricePerShare,
        String currency,
        String customerTier,
        boolean isInternational) {
}