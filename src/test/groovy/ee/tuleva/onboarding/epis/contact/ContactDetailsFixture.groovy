package ee.tuleva.onboarding.epis.contact

import static ee.tuleva.onboarding.epis.contact.UserPreferences.*

class ContactDetailsFixture {

    static UserPreferences contactDetailsFixture() {
        return builder()
            .firstName("Erko")
            .lastName("Risthein")
            .personalCode("38501010002")
            .addressRow1("Tuleva, Telliskivi 60")
            .country("EE")
            .postalIndex("10412")
            .districtCode("0784")
            .contactPreference(ContactPreferenceType.valueOf("E"))
            .languagePreference(LanguagePreferenceType.valueOf("EST"))
            .noticeNeeded("Y")
            .email("tuleva@tuleva.ee")
            .phoneNumber("+372546545")
            .pensionAccountNumber("993432432")
            .thirdPillarDistribution([new Distribution("EE123", 1.0)])
            .isSecondPillarActive(true)
            .isThirdPillarActive(true)
            .build()
    }
}
