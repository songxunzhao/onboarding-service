package ee.tuleva.onboarding.mandate

import com.codeborne.security.mobileid.IdCardSignatureSession
import com.codeborne.security.mobileid.MobileIdSignatureSession
import com.codeborne.security.mobileid.SignatureFile
import ee.tuleva.onboarding.fund.Fund
import ee.tuleva.onboarding.fund.FundRepository
import ee.tuleva.onboarding.mandate.command.CreateMandateCommand
import ee.tuleva.onboarding.mandate.command.CreateMandateCommandToMandateConverter
import ee.tuleva.onboarding.mandate.content.MandateContentCreator
import ee.tuleva.onboarding.mandate.content.MandateContentFile
import ee.tuleva.onboarding.mandate.exception.InvalidMandateException
import ee.tuleva.onboarding.mandate.signature.SignatureService
import ee.tuleva.onboarding.user.CsdUserPreferencesService
import ee.tuleva.onboarding.user.User
import ee.tuleva.onboarding.user.UserPreferences
import spock.lang.Specification

import static ee.tuleva.onboarding.auth.UserFixture.sampleUserPreferences
import static ee.tuleva.onboarding.mandate.MandateFixture.invalidCreateMandateCommand
import static ee.tuleva.onboarding.mandate.MandateFixture.sampleCreateMandateCommand

class MandateServiceSpec extends Specification {

    MandateRepository mandateRepository = Mock(MandateRepository)
    SignatureService signService = Mock(SignatureService)
    MandateContentCreator mandateContentCreator = Mock(MandateContentCreator)
    FundRepository fundRepository = Mock(FundRepository)
    CsdUserPreferencesService csdUserPreferencesService = Mock(CsdUserPreferencesService)
    CreateMandateCommandToMandateConverter converter = new CreateMandateCommandToMandateConverter()

    MandateService service = new MandateService(mandateRepository, signService, fundRepository,
            mandateContentCreator, csdUserPreferencesService, converter)

    Long sampleMandateId = 1L

    def "save: Converting create mandate command and persisting a mandate"() {
        given:
            1 * mandateRepository.save(_ as Mandate) >> { Mandate mandate ->
                return mandate
            }
            CreateMandateCommand createMandateCmd = sampleCreateMandateCommand()
        when:
            Mandate mandate = service.save(sampleUser(), createMandateCmd)
        then:
            mandate.futureContributionFundIsin == createMandateCmd.futureContributionFundIsin
            mandate.fundTransferExchanges.size() == createMandateCmd.fundTransferExchanges.size()
            mandate.fundTransferExchanges.first().sourceFundIsin ==
                    createMandateCmd.fundTransferExchanges.first().sourceFundIsin

            mandate.fundTransferExchanges.first().targetFundIsin ==
                    createMandateCmd.fundTransferExchanges.first().targetFundIsin

            mandate.fundTransferExchanges.first().amount ==
                    createMandateCmd.fundTransferExchanges.first().amount

    }

    def "save: Create mandate with invalid CreateMandateCommand fails"() {
        given:
        CreateMandateCommand createMandateCmd = invalidCreateMandateCommand()
        when:
        Mandate mandate = service.save(sampleUser(), createMandateCmd)
        then:
        thrown InvalidMandateException
    }

    def "getMandateFiles: generates mandate content files"() {
        given:
        User user = sampleUser()
        mockMandateFiles(user, sampleMandateId)

        when:
        List<SignatureFile> files = service.getMandateFiles(sampleMandateId, user)

        then:
        files.size() == 1
        files.get(0).mimeType == "html/text"
        files.get(0).content != null
    }

    def "mobile id signing works"() {
        given:
        def user = sampleUser()
        mockMandateFiles(user, sampleMandateId)
        1 * signService.startSign(_ as List<SignatureFile>, user.getPersonalCode(), user.getPhoneNumber()) >>
                new MobileIdSignatureSession(1, "1234")

        when:
        def session = service.mobileIdSign(sampleMandateId, user, user.getPhoneNumber())

        then:
        session.sessCode == 1
        session.challenge == "1234"
    }

    def "mobile id signature status works"() {
        given:
        1 * signService.getSignedFile(_) >> file
        mandateRepository.findOne(sampleMandateId) >> sampleMandate()
        mandateRepository.save({ Mandate it -> it.mandate == "file".getBytes() }) >> sampleMandate()

        when:
        def status = service.finalizeMobileIdSignature(sampleMandateId, new MobileIdSignatureSession(0, null))

        then:
        status == expectedStatus

        where:
        file          | expectedStatus
        null          | "OUTSTANDING_TRANSACTION"
        [0] as byte[] | "SIGNATURE"
    }

    def "mobile id signed mandate is saved"() {
        given:
        byte[] file = "file".getBytes()
        1 * signService.getSignedFile(_) >> file
        1 * mandateRepository.findOne(sampleMandateId) >> sampleMandate()
        1 * mandateRepository.save({ Mandate it -> it.mandate == file }) >> sampleMandate()

        when:
        service.finalizeMobileIdSignature(sampleMandateId, new MobileIdSignatureSession(0, null))

        then:
        true
    }

    def "id card signing works"() {
        given:
        def user = sampleUser()
        mockMandateFiles(user, sampleMandateId)

        signService.startSign(_ as List<SignatureFile>, "signingCertificate") >>
                new IdCardSignatureSession(1, "sigId", "hash")

        when:
        def session = service.idCardSign(sampleMandateId, user, "signingCertificate")

        then:
        session.sessCode == 1
        session.signatureId == "sigId"
        session.hash == "hash"
    }

    def "id card signed mandate is saved"() {
        given:
        def file = "file".getBytes()
        def session = new IdCardSignatureSession(1, "sigId", "hash")
        1 * signService.getSignedFile(session, "signedHash") >> file
        1 * mandateRepository.findOne(sampleMandateId) >> sampleMandate()

        when:
        service.finalizeIdCardSignature(sampleMandateId, session, "signedHash")

        then:
        1 * mandateRepository.save({ Mandate it -> it.mandate == file })
    }

    def "id card signature finalization throws exception when no signed file exist"() {
        given:
        def session = new IdCardSignatureSession(1, "sigId", "hash")
        1 * signService.getSignedFile(session, "signedHash") >> null

        when:
        service.finalizeIdCardSignature(sampleMandateId, session, "signedHash")

        then:
        thrown(IllegalStateException)
    }

    def mockMandateFiles(User user, Long mandateId) {
        1 * mandateRepository.findByIdAndUser(mandateId, user) >> sampleMandate()
        1 * fundRepository.findAll() >> [new Fund(), new Fund()]
        1 * csdUserPreferencesService.getPreferences(user.getPersonalCode()) >> sampleUserPreferences()

        1 * mandateContentCreator.
                getContentFiles(_ as User,
                        _ as Mandate,
                        _ as List,
                        _ as UserPreferences) >> [new MandateContentFile("file", "html/text", "file".getBytes())]
    }

    User sampleUser() {
        return User.builder()
                .personalCode("38501010002")
                .phoneNumber("5555555")
                .build()
    }

    Mandate sampleMandate() {
        Mandate.builder().build()
    }

}
