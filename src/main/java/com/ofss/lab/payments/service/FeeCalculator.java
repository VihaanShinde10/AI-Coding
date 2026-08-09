package com.ofss.lab.payments.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class FeeCalculator {

    // Fee schedule agreed with Product, March 2026
    static final double FLAT_FEE_CEILING = 1000.00;  // 1000.00 OR LESS pays flat
    static final double FLAT_FEE = 2.00;
    static final double STANDARD_RATE = 0.005;       // 0.5%
    static final double PREMIUM_RATE = 0.0025;       // 0.25%
    static final double STANDARD_MIN = 10.00, STANDARD_MAX = 250.00;
    static final double PREMIUM_MIN = 5.00, PREMIUM_MAX = 100.00;
    public static final double INTERNATIONAL_SURCHARGE = 15.00;

    /**
     * Calculates the total transaction fee, including the international
     * surcharge when applicable.
     *
     * @param amount          the transaction amount
     * @param customerTier    the customer tier (e.g. STANDARD or PREMIUM)
     * @param isInternational whether the transaction is international
     * @return the total fee rounded to 2 decimal places
     */
    public BigDecimal calculateTransactionFee(
            BigDecimal amount, String customerTier, boolean isInternational) {

        BigDecimal baseFee = calculateBaseFee(amount, customerTier);
        if (isInternational) {
            // Surcharge applies on top of the calculated fee, not instead of it.
            return baseFee.add(BigDecimal.valueOf(INTERNATIONAL_SURCHARGE))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return baseFee;
    }

    /**
     * Calculates the base transaction fee without the international surcharge.
     *
     * @param amount       the transaction amount
     * @param customerTier the customer tier (e.g. STANDARD or PREMIUM)
     * @return the base fee rounded to 2 decimal places
     */
    public BigDecimal calculateBaseFee(BigDecimal amount, String customerTier) {

        // Normalise the amount so the arithmetic below is consistent.
        double value = amount.doubleValue();

        double fee;
        if (value < FLAT_FEE_CEILING) {
            // Small transactions are charged the flat fee regardless of tier.
            fee = FLAT_FEE;
        } else if ("PREMIUM".equals(customerTier)) {
            fee = value * PREMIUM_RATE;
            fee = Math.max(PREMIUM_MIN, Math.min(fee, PREMIUM_MAX));
        } else {
            fee = value * STANDARD_RATE;
            fee = Math.max(STANDARD_MIN, Math.min(fee, STANDARD_MAX));
        }

        return BigDecimal.valueOf(fee).setScale(2, RoundingMode.HALF_UP);
    }
}