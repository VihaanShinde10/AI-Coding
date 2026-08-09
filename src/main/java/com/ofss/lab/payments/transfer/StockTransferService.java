package com.ofss.lab.payments.transfer;

import com.ofss.lab.payments.service.FeeCalculator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Executes a stock transfer by validating the request, computing the transfer
 * value, and calculating the transfer fee using the payment service's
 * {@link FeeCalculator}.
 */
@Service
public class StockTransferService {

    public static final String USD = "USD";
    public static final String INR = "INR";

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(USD, INR);

    private final FeeCalculator feeCalculator;

    public StockTransferService(FeeCalculator feeCalculator) {
        this.feeCalculator = feeCalculator;
    }

    /**
     * Executes a stock transfer and returns a report with the fee breakdown.
     *
     * @param request the stock transfer request
     * @return the stock transfer report
     * @throws IllegalArgumentException if the request is invalid
     */
    public StockTransferReport transfer(StockTransferRequest request) {
        validate(request);

        BigDecimal value = request.quantity().multiply(request.pricePerShare())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal baseFee = feeCalculator.calculateBaseFee(value, request.customerTier());
        BigDecimal surcharge = request.isInternational()
                ? BigDecimal.valueOf(FeeCalculator.INTERNATIONAL_SURCHARGE).setScale(2)
                : BigDecimal.ZERO.setScale(2);
        BigDecimal fee = feeCalculator.calculateTransactionFee(
                value, request.customerTier(), request.isInternational());

        return new StockTransferReport(
                request.sourceAccount(),
                request.destinationAccount(),
                request.symbol(),
                request.quantity(),
                request.pricePerShare(),
                request.currency(),
                request.customerTier(),
                request.isInternational(),
                baseFee,
                surcharge,
                fee);
    }

    private void validate(StockTransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        if (isBlank(request.sourceAccount()) || isBlank(request.destinationAccount())) {
            throw new IllegalArgumentException("Source and destination accounts must not be blank");
        }
        if (isBlank(request.symbol())) {
            throw new IllegalArgumentException("Symbol must not be blank");
        }
        if (request.quantity() == null || request.quantity().signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (request.pricePerShare() == null || request.pricePerShare().signum() <= 0) {
            throw new IllegalArgumentException("Price per share must be positive");
        }
        if (request.currency() == null || !SUPPORTED_CURRENCIES.contains(request.currency())) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + request.currency() + ". Supported currencies: " + SUPPORTED_CURRENCIES);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}