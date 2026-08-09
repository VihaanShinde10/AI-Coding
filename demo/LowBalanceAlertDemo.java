import com.ofss.lab.payments.alert.AccountBalance;
import com.ofss.lab.payments.alert.LowBalanceAlert;
import com.ofss.lab.payments.alert.LowBalanceAlertService;
import java.math.BigDecimal;
import java.util.List;

/**
 * Temporary demo that simulates the low-balance alert feature and prints
 * the alerts and formatted summaries for several scenarios.
 */
public class LowBalanceAlertDemo {

    public static void main(String[] args) {
        LowBalanceAlertService service = new LowBalanceAlertService();
        BigDecimal threshold = new BigDecimal("100.00");

        System.out.println("=== SCENARIO 1: Normal case (mixed balances) ===");
        List<AccountBalance> normal = List.of(
                new AccountBalance("ACC-001", new BigDecimal("50.00")),
                new AccountBalance("ACC-002", new BigDecimal("150.00")),
                new AccountBalance("ACC-003", new BigDecimal("99.99")));
        run(service, normal, threshold);

        System.out.println();
        System.out.println("=== SCENARIO 2: Boundary (exactly at threshold) ===");
        List<AccountBalance> boundary = List.of(
                new AccountBalance("ACC-001", new BigDecimal("100.00")));
        run(service, boundary, threshold);

        System.out.println();
        System.out.println("=== SCENARIO 3: Empty input ===");
        run(service, List.of(), threshold);

        System.out.println();
        System.out.println("=== SCENARIO 4: All below threshold ===");
        List<AccountBalance> allBelow = List.of(
                new AccountBalance("ACC-001", new BigDecimal("10.00")),
                new AccountBalance("ACC-002", new BigDecimal("20.00")));
        run(service, allBelow, threshold);

        System.out.println();
        System.out.println("=== SCENARIO 5: Zero and negative balances (reason codes) ===");
        List<AccountBalance> zeroNeg = List.of(
                new AccountBalance("ACC-001", BigDecimal.ZERO),
                new AccountBalance("ACC-002", new BigDecimal("-20.00")));
        run(service, zeroNeg, threshold);

        System.out.println();
        System.out.println("=== SCENARIO 6: Sorting by urgency then shortfall ===");
        List<AccountBalance> sorting = List.of(
                new AccountBalance("ACC-BT-1", new BigDecimal("80.00")),
                new AccountBalance("ACC-ZERO", BigDecimal.ZERO),
                new AccountBalance("ACC-NEG", new BigDecimal("-5.00")),
                new AccountBalance("ACC-BT-2", new BigDecimal("50.00")));
        run(service, sorting, threshold);

        System.out.println();
        System.out.println("=== SCENARIO 7: Validation (negative threshold) ===");
        try {
            service.evaluate(List.of(), new BigDecimal("-1.00"));
        } catch (IllegalArgumentException e) {
            System.out.println("Caught IllegalArgumentException: " + e.getMessage());
        }
    }

    private static void run(LowBalanceAlertService service,
                            List<AccountBalance> accounts, BigDecimal threshold) {
        List<LowBalanceAlert> alerts = service.evaluate(accounts, threshold);
        System.out.println("Alerts (" + alerts.size() + "):");
        for (LowBalanceAlert alert : alerts) {
            System.out.println("  " + alert);
        }
        System.out.println("Summary:");
        System.out.println(service.formatSummary(alerts));
    }
}