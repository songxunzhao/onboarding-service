package ee.tuleva.onboarding.comparisons.returns.provider;

import ee.tuleva.onboarding.auth.principal.Person;
import ee.tuleva.onboarding.comparisons.fundvalue.retrieval.EPIFundValueRetriever;
import ee.tuleva.onboarding.comparisons.overview.AccountOverview;
import ee.tuleva.onboarding.comparisons.overview.AccountOverviewProvider;
import ee.tuleva.onboarding.comparisons.returns.RateOfReturnCalculator;
import ee.tuleva.onboarding.comparisons.returns.Returns;
import ee.tuleva.onboarding.comparisons.returns.Returns.Return;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;

import static ee.tuleva.onboarding.comparisons.returns.Returns.Return.Type.INDEX;
import static java.util.Collections.singletonList;

@Service
@RequiredArgsConstructor
public class EPIReturnProvider implements ReturnProvider {

    private final AccountOverviewProvider accountOverviewProvider;

    private final RateOfReturnCalculator rateOfReturnCalculator;

    @Override
    public Returns getReturns(Person person, Instant startTime, Integer pillar) {
        AccountOverview accountOverview = accountOverviewProvider.getAccountOverview(person, startTime, pillar);
        double rateOfReturn = rateOfReturnCalculator.getRateOfReturn(accountOverview, EPIFundValueRetriever.KEY);

        Return theReturn = Return.builder()
            .key(EPIFundValueRetriever.KEY)
            .type(INDEX)
            .value(rateOfReturn)
            .build();

        return Returns.builder()
            .from(startTime.atZone(ZoneOffset.UTC).toLocalDate()) // TODO: Get real start time
            .returns(singletonList(theReturn))
            .build();
    }

}
