package ee.tuleva.onboarding.comparisons;

import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
public class Transaction {
    private BigDecimal amount;
    private Instant createdAt;
}
