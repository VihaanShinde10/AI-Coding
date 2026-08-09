package com.ofss.lab.payments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FeeCalculatorTest {

    private final FeeCalculator feeCalculator = new FeeCalculator();

    @Test
    void smallTransactionPaysFlatFeeRegardlessOfTier() {
        assertEquals(new BigDecimal("2.00"),
                feeCalculator.calculateTransactionFee(new BigDecimal("500.00"), "STANDARD", false));
        assertEquals(new BigDecimal("2.00"),
                feeCalculator.calculateTransactionFee(new BigDecimal("500.00"), "PREMIUM", false));
    }

    @Test
    void standardTierAppliesRateWithMinimum() {
        // 1000.00 * 0.5% = 5.00, but the standard minimum is 10.00.
        assertEquals(new BigDecimal("10.00"),
                feeCalculator.calculateTransactionFee(new BigDecimal("1000.00"), "STANDARD", false));
    }

    @Test
    void standardTierAppliesRateWithMaximum() {
        // 100000.00 * 0.5% = 500.00, but the standard maximum is 250.00.
        assertEquals(new BigDecimal("250.00"),
                feeCalculator.calculateTransactionFee(new BigDecimal("100000.00"), "STANDARD", false));
    }

    @Test
    void standardTierCalculatesMidRangeFee() {
        // 5000.00 * 0.5% = 25.00.
        assertEquals(new BigDecimal("25.00"),
                feeCalculator.calculateTransactionFee(new BigDecimal("5000.00"), "STANDARD", false));
    }

    @Test
    void premiumTierAppliesMinimumFee() {
        // 1000.00 * 0.25% = 2.50, but the premium minimum is 5.00.
        assertEquals(new BigDecimal("5.00"),
                feeCalculator.calculateTransactionFee(new BigDecimal("1000.00"), "PREMIUM", false));
    }

    @Test
    void premiumTierAppliesMaximumFee() {
        // 100000.00 * 0.25% = 250.00, but the premium maximum is 100.00.
        assertEquals(new BigDecimal("100.00"),
                feeCalculator.calculateTransactionFee(new BigDecimal("100000.00"), "PREMIUM", false));
    }

    @Test
    void premiumTierCalculatesMidRangeFee() {
        // 20000.00 * 0.25% = 50.00.
        assertEquals(new BigDecimal("50.00"),
                feeCalculator.calculateTransactionFee(new BigDecimal("20000.00"), "PREMIUM", false));
    }

    @Test
    void internationalSurchargeAppliesOnTopOfFlatFee() {
        // 2.00 flat + 15.00 surcharge = 17.00.
        assertEquals(new BigDecimal("17.00"),
                feeCalculator.calculateTransactionFee(new BigDecimal("500.00"), "STANDARD", true));
    }

    @Test
    void internationalSurchargeAppliesOnTopOfCalculatedFee() {
        // 25.00 standard + 15.00 surcharge = 40.00.
        assertEquals(new BigDecimal("40.00"),
                feeCalculator.calculateTransactionFee(new BigDecimal("5000.00"), "STANDARD", true));
    }

    @Test
    void feeIsRoundedToTwoDecimalPlaces() {
        // 3333.00 * 0.5% = 16.665 -> 16.67 (HALF_UP).
        assertEquals(new BigDecimal("16.67"),
                feeCalculator.calculateTransactionFee(new BigDecimal("3333.00"), "STANDARD", false));
        // 3333.00 * 0.25% = 8.3325 -> 8.33 (HALF_UP).
        assertEquals(new BigDecimal("8.33"),
                feeCalculator.calculateTransactionFee(new BigDecimal("3333.00"), "PREMIUM", false));
    }

    @Test
    void baseFeeExcludesInternationalSurcharge() {
        // 5000.00 * 0.5% = 25.00 base, no surcharge.
        assertEquals(new BigDecimal("25.00"),
                feeCalculator.calculateBaseFee(new BigDecimal("5000.00"), "STANDARD"));
        // 500.00 pays the flat fee of 2.00.
        assertEquals(new BigDecimal("2.00"),
                feeCalculator.calculateBaseFee(new BigDecimal("500.00"), "PREMIUM"));
    }
}
