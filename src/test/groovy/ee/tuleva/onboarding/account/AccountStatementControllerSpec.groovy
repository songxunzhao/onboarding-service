package ee.tuleva.onboarding.account

import ee.tuleva.domain.fund.FundRepository
import ee.tuleva.onboarding.xroad.XRoadClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AccountStatementController.class)
@AutoConfigureMockMvc
@WithMockUser
class AccountStatementControllerSpec extends Specification {

    @Autowired
    MockMvc mvc

    @MockBean
    XRoadClient xRoadClient;

    @MockBean
    FundRepository fundRepository;

    def "/pension-account-statement endpoint works"() {


        expect:
        mvc.perform(get("/v1/pension-account-statement"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$[1].isin', is("EE0987654321")))
    }

}
