package ee.tuleva.onboarding.mandate

import ee.tuleva.onboarding.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import spock.lang.Specification

import java.time.Instant

@DataJpaTest
class MandateRepositorySpec extends Specification {

    @Autowired
    private TestEntityManager entityManager

    @Autowired
    private MandateRepository repository

    def "persisting and findByIdAndUserId() works"() {
        given:
        def user = User.builder()
            .firstName("Erko")
            .lastName("Risthein")
            .personalCode("38501010002")
            .email("erko@risthein.ee")
            .phoneNumber("5555555")
            .createdDate(Instant.parse("2017-01-31T10:06:01Z"))
            .updatedDate(Instant.parse("2017-01-31T10:06:01Z"))
            .active(true)
            .build()
        entityManager.persist(user)
        entityManager.flush()

        def mandate = Mandate.builder()
            .user(user)
            .futureContributionFundIsin("isin")
            .fundTransferExchanges([])
            .pillar(2)
            .build()
        entityManager.persist(mandate)
        entityManager.flush()

        when:
        mandate = repository.findByIdAndUserId(mandate.id, user.id)

        then:
        mandate.user == user
        mandate.futureContributionFundIsin == Optional.of("isin")
        mandate.fundTransferExchanges == []
    }

}