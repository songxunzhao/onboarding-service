package ee.tuleva.onboarding.mandate

import ee.tuleva.onboarding.account.AccountStatementService
import ee.tuleva.onboarding.aml.AmlService
import ee.tuleva.onboarding.epis.EpisService
import ee.tuleva.onboarding.epis.contact.UserPreferences
import ee.tuleva.onboarding.error.response.ErrorResponse
import ee.tuleva.onboarding.error.response.ErrorsResponse
import ee.tuleva.onboarding.fund.Fund
import ee.tuleva.onboarding.fund.FundRepository
import ee.tuleva.onboarding.mandate.command.CreateMandateCommand
import ee.tuleva.onboarding.mandate.command.CreateMandateCommandToMandateConverter
import ee.tuleva.onboarding.mandate.content.MandateContentFile
import ee.tuleva.onboarding.mandate.exception.InvalidMandateException
import ee.tuleva.onboarding.mandate.listener.SecondPillarMandateCreatedEvent
import ee.tuleva.onboarding.mandate.processor.MandateProcessorService
import ee.tuleva.onboarding.mandate.signature.SignatureFile
import ee.tuleva.onboarding.mandate.signature.SignatureService
import ee.tuleva.onboarding.mandate.signature.idcard.IdCardSignatureSession
import ee.tuleva.onboarding.mandate.signature.mobileid.MobileIdSignatureSession
import ee.tuleva.onboarding.user.User
import ee.tuleva.onboarding.user.UserService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.servlet.LocaleResolver
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

import static ee.tuleva.onboarding.mandate.MandateFixture.*

class MandateServiceSpec extends Specification {

    MandateRepository mandateRepository = Mock(MandateRepository)
    SignatureService signService = Mock(SignatureService)
    FundRepository fundRepository = Mock()
    AccountStatementService accountStatementService = Mock()
    CreateMandateCommandToMandateConverter converter = new CreateMandateCommandToMandateConverter(accountStatementService, fundRepository)
    MandateProcessorService mandateProcessor = Mock(MandateProcessorService)
    MandateFileService mandateFileService = Mock(MandateFileService)
    UserService userService = Mock(UserService)
    EpisService episService = Mock(EpisService)
    AmlService amlService = Mock()
    ApplicationEventPublisher eventPublisher = Mock(ApplicationEventPublisher)
    HttpServletRequest request = Mock(HttpServletRequest)
    LocaleResolver localeResolver = Mock(LocaleResolver)

    MandateService service = new MandateService(mandateRepository, signService, converter, mandateProcessor,
        mandateFileService, userService, episService, amlService, eventPublisher, request, localeResolver)

    Long sampleMandateId = 1L
    User sampleUser = sampleUser()

    def setup() {
        userService.getById(sampleUser.id) >> sampleUser
    }

    def "save: Converting create mandate command and persisting a mandate"() {
        given:
        1 * mandateRepository.save(_ as Mandate) >> { Mandate mandate ->
            return mandate
        }
        CreateMandateCommand createMandateCmd = sampleCreateMandateCommand()
        when:
        Mandate mandate = service.save(sampleUser.id, createMandateCmd)
        then:
        mandate.futureContributionFundIsin == Optional.of(createMandateCmd.futureContributionFundIsin)
        mandate.fundTransferExchanges.size() == createMandateCmd.fundTransferExchanges.size()
        mandate.fundTransferExchanges.first().sourceFundIsin ==
            createMandateCmd.fundTransferExchanges.first().sourceFundIsin

        mandate.fundTransferExchanges.first().targetFundIsin ==
            createMandateCmd.fundTransferExchanges.first().targetFundIsin

        mandate.fundTransferExchanges.first().amount ==
            createMandateCmd.fundTransferExchanges.first().amount
        1 * episService.getContactDetails(sampleUser) >> UserPreferences.builder()
            .firstName(sampleUser.firstName)
            .lastName(sampleUser.lastName)
            .personalCode(sampleUser.personalCode)
            .build()
        1 * amlService.addPensionRegistryNameCheckIfMissing(sampleUser, _)
        1 * fundRepository.findByIsin(createMandateCmd.futureContributionFundIsin) >> Fund.builder().pillar(2).build()
        1 * amlService.allChecksPassed(_) >> true

    }

    def "save: Create mandate with invalid CreateMandateCommand fails"() {
        given:
        CreateMandateCommand createMandateCmd = invalidCreateMandateCommand()
        when:
        service.save(sampleUser.id, createMandateCmd)
        then:
        InvalidMandateException exception = thrown()
        exception.errorsResponse.errors.first().code == "invalid.mandate.source.amount.exceeded"
    }

    def "save: Create mandate with missing aml checks fails"() {
        given:
        CreateMandateCommand createMandateCmd = sampleCreateMandateCommand()
        when:
        service.save(sampleUser.id, createMandateCmd)
        then:
        InvalidMandateException exception = thrown()
        exception.errorsResponse.errors.first().code == "invalid.mandate.checks.missing"
        0 * mandateRepository.save(_)
        1 * amlService.allChecksPassed(_) >> false
        1 * fundRepository.findByIsin(createMandateCmd.futureContributionFundIsin) >> Fund.builder().pillar(2).build()
    }

    def "save: Create mandate with same source and target fund fails"() {
        given:
        CreateMandateCommand createMandateCmd = invalidCreateMandateCommandWithSameSourceAndTargetFund
        when:
        service.save(sampleUser.id, createMandateCmd)
        then:
        InvalidMandateException exception = thrown()
        exception.errorsResponse.errors.first().code == "invalid.mandate.same.source.and.target.transfer.present"
    }

    def "mobile id signing works"() {
        given:
        def user = sampleUser()
        def signatureSession = MobileIdSignatureSession.builder().build()

        1 * mandateFileService.getMandateFiles(sampleMandateId, user.id) >> sampleFiles()
        1 * signService.startMobileIdSign(_ as List<SignatureFile>, user.personalCode, user.phoneNumber) >>
            signatureSession

        when:
        def session = service.mobileIdSign(sampleMandateId, user.id, user.phoneNumber)

        then:
        session == signatureSession
    }

    def "finalizeMobileIdSignature: get correct status if currently signing mandate"() {
        given:
        Mandate sampleMandate = sampleUnsignedMandate()
        def signatureSession = MobileIdSignatureSession.builder().build()

        1 * mandateRepository.findByIdAndUserId(sampleMandate.id, sampleUser.id) >> sampleMandate
        1 * signService.getSignedFile(_) >> null

        when:
        def status = service.finalizeMobileIdSignature(sampleUser.id, sampleMandate.id, signatureSession)

        then:
        status == "OUTSTANDING_TRANSACTION"
    }

    def "finalizeMobileIdSignature: get correct status if currently signed a mandate and start processing"() {
        given:
        Mandate sampleMandate = sampleUnsignedMandate()
        byte[] sampleFile = "file".getBytes()
        def signatureSession = MobileIdSignatureSession.builder().build()

        1 * mandateRepository.findByIdAndUserId(sampleMandate.id, sampleUser.id) >> sampleMandate
        1 * signService.getSignedFile(_) >> sampleFile
        1 * mandateRepository.save({ Mandate it -> it.mandate.get() == sampleFile }) >> sampleMandate

        when:
        def status = service.finalizeMobileIdSignature(sampleUser.id, sampleMandate.id, signatureSession)

        then:
        1 * mandateProcessor.start(sampleUser, sampleMandate)
        status == "OUTSTANDING_TRANSACTION"
    }

    def "finalizeMobileIdSignature: get correct status if mandate is signed and being processed"() {
        given:
        Mandate sampleMandate = sampleMandate()
        def signatureSession = MobileIdSignatureSession.builder().build()

        1 * mandateRepository.findByIdAndUserId(sampleMandate.id, sampleUser.id) >> sampleMandate
        1 * mandateProcessor.isFinished(sampleMandate) >> false
        0 * eventPublisher.publishEvent(_ as SecondPillarMandateCreatedEvent)

        when:
        def status = service.finalizeMobileIdSignature(sampleUser.id, sampleMandate.id, signatureSession)

        then:
        status == "OUTSTANDING_TRANSACTION"
    }

    def "finalizeMobileIdSignature: get correct status and notify and invalidate EPIS cache if mandate is signed and processed"() {
        given:
        Mandate sampleMandate = sampleMandate()
        def signatureSession = MobileIdSignatureSession.builder().build()

        1 * mandateRepository.findByIdAndUserId(sampleMandate.id, sampleUser.id) >> sampleMandate
        1 * mandateProcessor.isFinished(sampleMandate) >> true
        1 * mandateProcessor.getErrors(sampleMandate) >> sampleEmptyErrorsResponse
        1 * episService.clearCache(sampleUser)

        when:
        def status = service.finalizeMobileIdSignature(sampleUser.id, sampleMandate.id, signatureSession)

        then:
        status == "SIGNATURE"
        1 * eventPublisher.publishEvent({
            SecondPillarMandateCreatedEvent event = it as SecondPillarMandateCreatedEvent
            event.user == sampleUser
            event.mandateId == sampleMandate.id
        })
    }

    def "finalizeMobileIdSignature: throw exception if mandate is signed and processed and has errors"() {
        given:
        Mandate sampleMandate = sampleMandate()
        def signatureSession = MobileIdSignatureSession.builder().build()

        1 * mandateRepository.findByIdAndUserId(sampleMandate.id, sampleUser.id) >> sampleMandate
        1 * mandateProcessor.isFinished(sampleMandate) >> true
        1 * mandateProcessor.getErrors(sampleMandate) >> sampleErrorsResponse
        0 * eventPublisher.publishEvent(_ as SecondPillarMandateCreatedEvent)

        when:
        service.finalizeMobileIdSignature(sampleUser.id, sampleMandate.id, signatureSession)

        then:
        thrown InvalidMandateException

    }

    def "id card signing works"() {
        given:
        def user = sampleUser()
        def signatureSession = IdCardSignatureSession.builder().build()

        1 * mandateFileService.getMandateFiles(sampleMandateId, user.id) >> sampleFiles()
        1 * signService.startIdCardSign(_ as List<SignatureFile>, "signingCertificate") >>
           signatureSession

        when:
        def session = service.idCardSign(sampleMandateId, user.id, "signingCertificate")

        then:
        session == signatureSession
    }

    def "finalizeIdCardSignature: throws exception when no signed file exist"() {
        given:
        Mandate sampleMandate = sampleUnsignedMandate()
        def signatureSession = IdCardSignatureSession.builder().build()

        1 * mandateRepository.findByIdAndUserId(sampleMandate.id, sampleUser.id) >> sampleMandate
        1 * signService.getSignedFile(signatureSession, "signedHash") >> null
        0 * eventPublisher.publishEvent(_ as SecondPillarMandateCreatedEvent)

        when:
        service.finalizeIdCardSignature(sampleUser.id, sampleMandate.id, signatureSession, "signedHash")

        then:
        thrown(IllegalStateException)
    }

    def "finalizeIdCardSignature: get correct status if currently signed a mandate and start processing"() {
        given:
        Mandate sampleMandate = sampleUnsignedMandate()
        def signatureSession = IdCardSignatureSession.builder().build()
        byte[] sampleFile = "file".getBytes()
        1 * signService.getSignedFile(signatureSession, "signedHash") >> sampleFile
        1 * mandateRepository.findByIdAndUserId(sampleMandate.id, sampleUser.id) >> sampleMandate
        1 * mandateRepository.save({ Mandate it -> it.mandate.get() == sampleFile }) >> sampleMandate
        0 * eventPublisher.publishEvent(_ as SecondPillarMandateCreatedEvent)

        when:
        def status = service.finalizeIdCardSignature(sampleUser.id, sampleMandate.id, signatureSession, "signedHash")

        then:
        1 * mandateProcessor.start(sampleUser, sampleMandate)
        status == "OUTSTANDING_TRANSACTION"
    }

    def "finalizeIdCardSignature: get correct status if mandate is signed and being processed"() {
        given:
        Mandate sampleMandate = sampleMandate()
        def signatureSession = IdCardSignatureSession.builder().build()

        1 * mandateRepository.findByIdAndUserId(sampleMandate.id, sampleUser.id) >> sampleMandate
        1 * mandateProcessor.isFinished(sampleMandate) >> false
        0 * eventPublisher.publishEvent(_ as SecondPillarMandateCreatedEvent)

        when:
        def status = service.finalizeIdCardSignature(sampleUser.id, sampleMandate.id, signatureSession, "signedHash")

        then:
        status == "OUTSTANDING_TRANSACTION"
    }

    def "finalizeIdCardSignature: get correct status and notify and invalidate EPIS cache if mandate is signed and processed"() {
        given:
        Mandate sampleMandate = sampleMandate()
        def signatureSession = IdCardSignatureSession.builder().build()

        1 * mandateRepository.findByIdAndUserId(sampleMandate.id, sampleUser.id) >> sampleMandate
        1 * mandateProcessor.isFinished(sampleMandate) >> true
        1 * mandateProcessor.getErrors(sampleMandate) >> sampleEmptyErrorsResponse
        1 * episService.clearCache(sampleUser)

        when:
        def status = service.finalizeIdCardSignature(sampleUser.id, sampleMandate.id, signatureSession, "signedHash")

        then:
        status == "SIGNATURE"
        1 * eventPublisher.publishEvent({
            SecondPillarMandateCreatedEvent event = it as SecondPillarMandateCreatedEvent
            event.user == sampleUser
            event.mandateId == sampleMandate.id
        })
    }

    def "finalizeIdCardSignature: throw exception if mandate is signed and processed and has errors"() {
        given:
        Mandate sampleMandate = sampleMandate()
        def signatureSession = IdCardSignatureSession.builder().build()

        1 * mandateRepository.findByIdAndUserId(sampleMandate.id, sampleUser.id) >> sampleMandate
        1 * mandateProcessor.isFinished(sampleMandate) >> true
        1 * mandateProcessor.getErrors(sampleMandate) >> sampleErrorsResponse
        0 * eventPublisher.publishEvent(_ as SecondPillarMandateCreatedEvent)

        when:
        service.finalizeIdCardSignature(sampleUser.id, sampleMandate.id, signatureSession, "signedHash")

        then:
        thrown InvalidMandateException

    }

    User sampleUser() {
        return User.builder()
            .personalCode("38501010002")
            .phoneNumber("5555555")
            .build()
    }

    ErrorsResponse sampleErrorsResponse = new ErrorsResponse([ErrorResponse.builder().code('sampe.error').build()])
    ErrorsResponse sampleEmptyErrorsResponse = new ErrorsResponse([])

    private List<MandateContentFile> sampleFiles() {
        return [new MandateContentFile("file", "html/text", "file".getBytes())]
    }

}
