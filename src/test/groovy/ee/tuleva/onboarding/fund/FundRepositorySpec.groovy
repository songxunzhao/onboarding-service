package ee.tuleva.onboarding.fund

import ee.tuleva.onboarding.fund.manager.FundManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import spock.lang.Specification

import static ee.tuleva.onboarding.fund.Fund.FundStatus.ACTIVE
import static ee.tuleva.onboarding.fund.Fund.FundStatus.LIQUIDATED
import static java.util.stream.Collectors.toList
import static java.util.stream.StreamSupport.stream

@DataJpaTest
class FundRepositorySpec extends Specification {

    @Autowired
    private TestEntityManager entityManager

    @Autowired
    private FundRepository repository

    def "persisting and finding by fund manager name works"() {
        given:
        def fundManager = FundManager.builder()
            .id(1)
            .name("Tuleva")
            .build()
        def fund = Fund.builder()
            .isin("EE000000000")
            .nameEstonian("Tuleva Maailma Aktsiate Pensionifond")
            .nameEnglish("Tuleva Maailma Aktsiate Pensionifond")
            .pillar(2)
            .equityShare(0.0)
            .managementFeeRate(new BigDecimal("0.0034"))
            .ongoingChargesFigure(new BigDecimal("0.005"))
            .status(ACTIVE)
            .fundManager(fundManager)
            .build()
        entityManager.persist(fund)
        entityManager.flush()

        when:
        Iterable<Fund> funds = repository.findByFundManagerNameIgnoreCase("Tuleva")
        Fund persistedFund = stream(funds.spliterator(), false)
            .filter({ f -> f.isin == fund.isin })
            .findFirst()
            .get()

        then:
        persistedFund.id != null
        persistedFund.isin == fund.isin
        persistedFund.nameEstonian == fund.nameEstonian
        persistedFund.nameEnglish == fund.nameEnglish
        persistedFund.pillar == fund.pillar
        persistedFund.managementFeeRate == fund.managementFeeRate
        persistedFund.ongoingChargesFigure == fund.ongoingChargesFigure
        persistedFund.status == fund.status
        persistedFund.fundManager == fundManager
    }

    def "finding by pillar works"() {
        given:
        def fundManager = FundManager.builder()
            .id(1)
            .name("Tuleva")
            .build()
        def fund = Fund.builder()
            .isin("EE000000000")
            .nameEstonian("Tuleva Maailma Aktsiate Pensionifond")
            .nameEnglish("Tuleva Maailma Aktsiate Pensionifond")
            .pillar(2)
            .equityShare(0.0)
            .managementFeeRate(new BigDecimal("0.0034"))
            .ongoingChargesFigure(new BigDecimal("0.005"))
            .status(ACTIVE)
            .fundManager(fundManager)
            .build()
        entityManager.persist(fund)
        entityManager.flush()

        when:
        Iterable<Fund> funds = repository.findAllByPillar(2)
        Fund persistedFund = stream(funds.spliterator(), false)
            .filter({ f -> f.isin == fund.isin })
            .findFirst()
            .get()

        then:
        persistedFund.id != null
        persistedFund.isin == fund.isin
        persistedFund.nameEstonian == fund.nameEstonian
        persistedFund.nameEnglish == fund.nameEnglish
        persistedFund.pillar == fund.pillar
        persistedFund.managementFeeRate == fund.managementFeeRate
        persistedFund.ongoingChargesFigure == fund.ongoingChargesFigure
        persistedFund.status == fund.status
        persistedFund.fundManager == fundManager

        when:
        Iterable<Fund> thirdPillarFunds = repository.findAllByPillar(3)

        then:
        thirdPillarFunds.size() == 14 // TODO: bad assert, depends on the flyway migrations
    }

    def "ignores inactive funds"() {
        given:
        def fundManager = FundManager.builder()
            .id(1)
            .name("Tuleva")
            .build()
        def activeFund = Fund.builder()
            .isin("EE000000000")
            .nameEstonian("Tuleva Maailma Aktsiate Pensionifond")
            .nameEnglish("Tuleva Maailma Aktsiate Pensionifond")
            .pillar(2)
            .equityShare(0.0)
            .managementFeeRate(new BigDecimal("0.0034"))
            .ongoingChargesFigure(new BigDecimal("0.005"))
            .status(ACTIVE)
            .fundManager(fundManager)
            .build()
        def inactiveFund = Fund.builder()
            .isin("EE000000002")
            .nameEstonian("Vana Fond")
            .nameEnglish("Some Old Fund")
            .pillar(2)
            .equityShare(0.0)
            .managementFeeRate(new BigDecimal("0.0123"))
            .ongoingChargesFigure(new BigDecimal("0.0123"))
            .status(LIQUIDATED)
            .fundManager(fundManager)
            .build()
        entityManager.persist(activeFund)
        entityManager.persist(inactiveFund)
        entityManager.flush()

        when:
        Iterable<Fund> funds = repository.findByFundManagerNameIgnoreCase("Tuleva")
        List<Fund> inactiveFunds = stream(funds.spliterator(), false)
            .filter({ fund -> fund.status != ACTIVE })
            .collect(toList())

        then:
        inactiveFunds == []
    }
}