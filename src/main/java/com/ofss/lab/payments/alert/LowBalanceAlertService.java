package com.ofss.lab.payments.alert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Evaluates account balances against a low-balance threshold and produces
 * alerts, sorted by urgency.
 */
@Service
public class LowBalanceAlertService {

    public static final String REASON_BELOW_THRESHOLD = "BELOW_THRESHOLD";
    public static final String REASON_ZERO_BALANCE = "ZERO_BALANCE";
    public static final String REASON_NEGATIVE_BALANCE = "NEGATIVE_BALANCE";

    /**
     * Evaluates the given account balances against the threshold and returns
     * the alerts sorted by urgency: negative balance first, then zero balance,
     * then below threshold; within the same reason, larger shortfall first.
     *
     * @param accounts  the account balances (may be empty)
     * @param threshold the low-balance threshold (must be non-null and non-negative)
     * @return the low-balance alerts, sorted by urgency
     * @throws IllegalArgumentException if the threshold is null or negative, or
     *                                  if any balance is null
     */
    public List<LowBalanceAlert> evaluate(List<AccountBalance> accounts, BigDecimal threshold) {
        validateThreshold(threshold);

        if (accounts.isEmpty()) {
            return List.of();
        }

        return accounts.stream()
                .map(account -> toAlert(account, threshold))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator
                        .comparingInt((LowBalanceAlert alert) -> urgency(alert.reasonCode()))
                        .thenComparing(LowBalanceAlert::shortfall, Comparator.reverseOrder()))
                .toList();
    }

    /**
     * Formats the alerts as a text summary.
     *
     * @param alerts the low-balance alerts
     * @return the formatted summary
     */
    public String formatSummary(List<LowBalanceAlert> alerts) {
        StringBuilder summary = new StringBuilder("Low balance alerts: ").append(alerts.size());
        for (LowBalanceAlert alert : alerts) {
            summary.append(System.lineSeparator())
                    .append("- ").append(alert.accountId())
                    .append(": balance ").append(alert.balance().toPlainString())
                    .append(", threshold ").append(alert.threshold().toPlainString())
                    .append(", shortfall ").append(alert.shortfall().toPlainString())
                    .append(" [").append(alert.reasonCode()).append("]");
        }
        return summary.toString();
    }

    private LowBalanceAlert toAlert(AccountBalance account, BigDecimal threshold) {
        validateBalance(account);
        BigDecimal balance = account.balance();
        int comparison = balance.compareTo(threshold);
        if (comparison >= 0) {
            // Balance is at or above the threshold: no alert.
            return null;
        }

        BigDecimal shortfall = threshold.subtract(balance).setScale(2, RoundingMode.HALF_UP);
        String reasonCode;
        if (balance.signum() < 0) {
            reasonCode = REASON_NEGATIVE_BALANCE;
        } else if (balance.signum() == 0) {
            reasonCode = REASON_ZERO_BALANCE;
        } else {
            reasonCode = REASON_BELOW_THRESHOLD;
        }

        return new LowBalanceAlert(
                account.accountId(),
                balance,
                threshold,
                shortfall,
                reasonCode);
    }

    private int urgency(String reasonCode) {
        return switch (reasonCode) {
            case REASON_NEGATIVE_BALANCE -> 0;
            case REASON_ZERO_BALANCE -> 1;
            case REASON_BELOW_THRESHOLD -> 2;
            default -> 3;
        };
    }

    private void validateThreshold(BigDecimal threshold) {
        if (threshold == null) {
            throw new IllegalArgumentException("Threshold must not be null");
        }
        if (threshold.signum() < 0) {
            throw new IllegalArgumentException("Threshold must not be negative");
        }
    }

    private void validateBalance(AccountBalance account) {
        if (account == null) {
            throw new IllegalArgumentException("Account balance must not be null");
        }
        if (account.balance() == null) {
            throw new IllegalArgumentException("Balance must not be null for account " + account.accountId());
        }
    }
}