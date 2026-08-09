package com.ofss.lab.payments.transfer;

import org.springframework.stereotype.Service;

/**
 * Formats a {@link StockTransferReport} into a single CSV line so stock
 * transfer reports can be written to a file or streamed to a consumer.
 */
@Service
public class StockTransferReportGenerator {

    private static final String HEADER =
            "sourceAccount,destinationAccount,symbol,quantity,pricePerShare,currency,customerTier,isInternational,baseFee,surcharge,fee";

    /**
     * Returns the CSV header line.
     *
     * @return the header line
     */
    public String header() {
        return HEADER;
    }

    /**
     * Formats a stock transfer report as a CSV line.
     *
     * @param report the stock transfer report
     * @return the CSV line
     */
    public String toCsv(StockTransferReport report) {
        return String.join(",",
                report.sourceAccount(),
                report.destinationAccount(),
                report.symbol(),
                report.quantity().toPlainString(),
                report.pricePerShare().toPlainString(),
                report.currency(),
                report.customerTier(),
                String.valueOf(report.isInternational()),
                report.baseFee().toPlainString(),
                report.surcharge().toPlainString(),
                report.fee().toPlainString());
    }
}