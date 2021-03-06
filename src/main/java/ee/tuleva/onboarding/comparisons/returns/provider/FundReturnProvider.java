package ee.tuleva.onboarding.comparisons.returns.provider;

import ee.tuleva.onboarding.auth.principal.Person;
import ee.tuleva.onboarding.comparisons.overview.AccountOverview;
import ee.tuleva.onboarding.comparisons.overview.AccountOverviewProvider;
import ee.tuleva.onboarding.comparisons.returns.RateOfReturnCalculator;
import ee.tuleva.onboarding.comparisons.returns.Returns;
import ee.tuleva.onboarding.comparisons.returns.Returns.Return;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;

import static ee.tuleva.onboarding.comparisons.returns.Returns.Return.Type.FUND;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class FundReturnProvider implements ReturnProvider {

    private final AccountOverviewProvider accountOverviewProvider;

    private final RateOfReturnCalculator rateOfReturnCalculator;

    @Override
    public Returns getReturns(Person person, Instant startTime, Integer pillar) {
        AccountOverview accountOverview = accountOverviewProvider.getAccountOverview(person, startTime, pillar);

        List<Return> returns = getKeys().stream()
            .map(key -> new SimpleEntry<>(key, rateOfReturnCalculator.getRateOfReturn(accountOverview, key)))
            .map(tuple -> Return.builder()
                .key(tuple.getKey())
                .type(FUND)
                .value(tuple.getValue())
                .build()
            ).collect(toList());

        return Returns.builder()
            .from(startTime.atZone(ZoneOffset.UTC).toLocalDate()) // TODO: Get real start time
            .returns(returns)
            .build();
    }

    @Override
    public List<String> getKeys() {
        return Arrays.asList(
            "EE3600019774",
            "EE3600019832",
            "EE3600019824",
            "EE3600019782",
            "EE3600019717",
            "EE3600019733",
            "EE3600098612",
            "EE3600019725",
            "EE3600019758",
            "EE3600019741",
            "EE3600019766"
        );
    }

}
