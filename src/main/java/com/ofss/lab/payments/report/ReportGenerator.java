package com.ofss.lab.payments.report;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Formats {@link PaymentReport} instances into CSV lines so reports can be
 * written to a file or streamed to a consumer.
 */
@Service
public class ReportGenerator {

    private static final String HEADER =
            "currency,amount,customerTier,isInternational,baseFee,surcharge,fee";

    /**
     * Returns the CSV header line.
     *
     * @return the header line
     */
    public String header() {
        return HEADER;
    }

    /**
     * Formats a single report as a CSV line.
     *
     * @param report the payment report
     * @return the CSV line
     */
    public String toCsv(PaymentReport report) {
        return String.join(",",
                report.currency(),
                report.amount().toPlainString(),
                report.customerTier(),
                String.valueOf(report.isInternational()),
                report.baseFee().toPlainString(),
                report.surcharge().toPlainString(),
                report.fee().toPlainString());
    }

    /**
     * Formats a batch of reports as a CSV document with a header line followed
     * by one line per report.
     *
     * @param reports the payment reports
     * @return the full CSV document
     */
    public String toCsvBatch(List<PaymentReport> reports) {
        StringBuilder csv = new StringBuilder(header());
        for (PaymentReport report : reports) {
            csv.append(System.lineSeparator()).append(toCsv(report));
        }
        return csv.toString();
    }
}