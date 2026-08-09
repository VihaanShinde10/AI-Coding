package com.ofss.lab.payments.report;

import com.ofss.lab.payments.service.FeeCalculator;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Builds a {@link PaymentReport} for a single transaction by delegating the
 * fee calculation to the payment service's {@link FeeCalculator}.
 *
 * <p>The currency is captured on the report for information only; it does not
 * affect the fee calculation. Only the supported currencies are accepted.</p>
 */
@Service
public class PaymentReportService {

    public static final String USD = "USD";
    public static final String INR = "INR";

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(USD, INR);

    private final FeeCalculator feeCalculator;

    public PaymentReportService(FeeCalculator feeCalculator) {
        this.feeCalculator = feeCalculator;
    }

    /**
     * Builds a payment report for a single transaction.
     *
     * @param amount          the transaction amount
     * @param currency        the transaction currency (must be USD or INR)
     * @param customerTier    the customer tier (e.g. STANDARD or PREMIUM)
     * @param isInternational whether the transaction is international
     * @return the payment report including the fee breakdown
     * @throws IllegalArgumentException if the currency is not supported
     */
    public PaymentReport buildReport(
            BigDecimal amount, String currency, String customerTier, boolean isInternational) {

        validateCurrency(currency);

        BigDecimal baseFee = feeCalculator.calculateBaseFee(amount, customerTier);
        BigDecimal surcharge = isInternational
                ? BigDecimal.valueOf(FeeCalculator.INTERNATIONAL_SURCHARGE).setScale(2)
                : BigDecimal.ZERO.setScale(2);
        BigDecimal fee = feeCalculator.calculateTransactionFee(
                amount, customerTier, isInternational);

        return new PaymentReport(
                amount, currency, customerTier, isInternational, baseFee, surcharge, fee);
    }

    private void validateCurrency(String currency) {
        if (currency == null || !SUPPORTED_CURRENCIES.contains(currency)) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + currency + ". Supported currencies: " + SUPPORTED_CURRENCIES);
        }
    }
}