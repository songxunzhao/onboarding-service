package ee.tuleva.onboarding.mandate.transfer

import ee.tuleva.onboarding.BaseControllerSpec
import ee.tuleva.onboarding.auth.principal.Person
import ee.tuleva.onboarding.epis.mandate.MandateApplicationStatus
import ee.tuleva.onboarding.fund.Fund
import org.hamcrest.Matchers
import org.springframework.test.web.servlet.MockMvc

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TransferExchangeControllerSpec extends BaseControllerSpec {

    TransferExchangeService transferExchangeService = Mock(TransferExchangeService)

    TransferExchangeController controller = new TransferExchangeController(transferExchangeService)

    MockMvc mockMvc

    def setup() {
        mockMvc = mockMvc(controller)
    }

    def "/transfer-exchanges endpoint works"() {
        given:
        1 * transferExchangeService.get(_ as Person) >> sampleTransfersApplicationList

        expect:
        mockMvc.perform(get('/v1/transfer-exchanges')
            .header('Accept-Language', language)
            .param('status', 'PENDING'))
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.*', Matchers.hasSize(1)))
            .andExpect(jsonPath('$[0].sourceFund.name', is(srcTranslation)))
            .andExpect(jsonPath('$[0].targetFund.name', is(targetTranslation)))
        where:
        language | srcTranslation      | targetTranslation
        'et'     | 'src fund name est' | 'target fund name est'
        'en'     | 'src fund name eng' | 'target fund name eng'
    }

    Fund sourceFund = Fund.builder()
        .nameEnglish("src fund name eng")
        .nameEstonian("src fund name est")
        .build()

    Fund targetFund = Fund.builder()
        .nameEnglish("target fund name eng")
        .nameEstonian("target fund name est")
        .build()

    List<TransferExchange> sampleTransfersApplicationList = [
        TransferExchange.builder()
            .status(MandateApplicationStatus.FAILED)
            .build(),
        TransferExchange.builder()
            .status(MandateApplicationStatus.COMPLETE)
            .build(),
        TransferExchange.builder()
            .status(MandateApplicationStatus.PENDING)
            .sourceFund(sourceFund)
            .targetFund(targetFund)
            .build()
    ]

}
