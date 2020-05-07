package ee.tuleva.onboarding.mandate.listener;

import ee.tuleva.onboarding.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ThirdPillarMandateCreatedEvent extends MandateCreatedEvent {
    private final User user;
    private final Long mandateId;
    private final byte[] signedFile;
    private final String pensionAccountNumber;
}
