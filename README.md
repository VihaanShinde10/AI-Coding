# Payments Reporting Module

A minimal reporting module for the payment service. It wraps the payment
service's `FeeCalculator` and produces simple, machine-readable payment
reports (one line per transaction) that can be written to a file or streamed
to a consumer.

## Purpose

The payment service calculates transaction fees based on a fee schedule agreed
with Product in March 2026:

- Transactions of **1000.00 or less** pay a flat fee of **2.00**, regardless of
  customer tier.
- **STANDARD** tier: 0.5% of the amount, capped between **10.00** and **250.00**.
- **PREMIUM** tier: 0.25% of the amount, capped between **5.00** and **100.00**.
- **International** transactions add a **15.00** surcharge on top of the
  calculated fee.

This module captures the inputs (including the transaction currency) and the
calculated fee for each transaction and formats them as CSV, so downstream
systems can consume the data without re-implementing the fee logic.

Two currencies are supported and captured on the report for information only —
they do not affect the fee calculation: **USD** and **INR**. Unsupported
currencies are rejected with an `IllegalArgumentException`.

The fee is broken down into the **base fee** (without the international
surcharge) and the **surcharge** applied on top, so consumers can see exactly
how the total fee was derived.

## Structure

```
.
├── pom.xml
├── README.md
└── src
    ├── main
    │   └── java/com/ofss/lab/payments
    │       ├── service
    │       │   └── FeeCalculator.java        # Payment fee calculation logic
    │       └── report
    │           ├── PaymentReport.java        # Immutable data holder (record, incl. currency & fee breakdown)
    │           ├── PaymentReportService.java # Builds a report via FeeCalculator (USD/INR, validates currency)
    │           └── ReportGenerator.java      # Formats reports as CSV (single or batch)
    └── test
        └── java/com/ofss/lab/payments
            ├── service
            │   └── FeeCalculatorTest.java    # Unit tests for fee logic
            └── report
                ├── PaymentReportServiceTest.java
                └── ReportGeneratorTest.java
```

### Classes

| Class | Responsibility |
| --- | --- |
| `FeeCalculator` | Calculates the transaction fee from amount, tier, and international flag. |
| `PaymentReport` | Immutable record holding amount, currency, customer tier, international flag, base fee, surcharge, and total fee. |
| `PaymentReportService` | `@Service` that builds a `PaymentReport` by delegating to `FeeCalculator`; exposes `USD`/`INR` constants and validates the currency. |
| `ReportGenerator` | `@Service` that formats a `PaymentReport` into a CSV line, or a batch of reports into a full CSV document. |

## How to run

The module is a standard Maven project using Spring Boot. It is a library-style
module with no web endpoints and no `main` class, so it is built as a plain jar
and consumed as a dependency by other services.

```bash
# Build the project (compiles main and test sources, produces the jar)
mvn package
```

The resulting jar is produced at `target/payments-reporting-1.0.0.jar`. To use
the reporting beans (`PaymentReportService`, `ReportGenerator`) in another
Spring Boot application, add this module as a dependency and inject the beans
as usual.

## Stock Transfer Feature

The module also provides a stock-transfer capability that moves shares between
two accounts and calculates the transfer fee using the same fee schedule as
`FeeCalculator`.

### Classes

| Class | Responsibility |
| --- | --- |
| `StockTransferRequest` | Immutable record holding the transfer inputs (accounts, symbol, quantity, price per share, currency, tier, international flag). |
| `StockTransferReport` | Immutable record holding the transfer inputs plus the fee breakdown (base fee, surcharge, total fee). |
| `StockTransferService` | `@Service` that validates the request, computes the transfer value (quantity × price per share), and calculates the fee via `FeeCalculator`. |
| `StockTransferReportGenerator` | `@Service` that formats a `StockTransferReport` into a single CSV line. |

The transfer value is `quantity × pricePerShare`, rounded to 2 decimal places
with `RoundingMode.HALF_UP`, and is passed to `FeeCalculator` for the fee.
Validation rejects null requests, blank accounts/symbol, non-positive quantity
or price, and unsupported currencies.

## Low-Balance Alert Feature

The module also provides a low-balance alert capability that evaluates account
balances against a configured threshold and produces alerts sorted by urgency.

### Classes

| Class | Responsibility |
| --- | --- |
| `AccountBalance` | Immutable record holding an account ID and its current balance. |
| `LowBalanceAlert` | Immutable record holding the account, balance, threshold, shortfall, and reason code. |
| `LowBalanceAlertService` | `@Service` that evaluates balances against a threshold, sorts alerts by urgency, and formats a text summary. |

Alerts are produced only for accounts whose balance is **below** the threshold
(a balance exactly equal to the threshold produces no alert). Reason codes:

| Code | Meaning |
| --- | --- |
| `NEGATIVE_BALANCE` | Balance is negative (overdrawn) — most urgent. |
| `ZERO_BALANCE` | Balance is exactly zero. |
| `BELOW_THRESHOLD` | Balance is positive but below the threshold. |

Alerts are sorted by urgency (negative first, then zero, then below-threshold)
and, within the same reason code, by shortfall descending (larger shortfall =
more urgent). Empty input produces no alerts, and a summary of an empty alert
list renders as `Low balance alerts: 0`.

## How to run its tests

Tests use JUnit 5 (via `spring-boot-starter-test`).

```bash
# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=FeeCalculatorTest
```

## Conventions

- Package root: `com.ofss.lab.payments`.
- Services are annotated with `@Service` and use constructor injection.
- Money is handled with `BigDecimal`, rounded to 2 decimal places with
  `RoundingMode.HALF_UP`, consistent with `FeeCalculator`.