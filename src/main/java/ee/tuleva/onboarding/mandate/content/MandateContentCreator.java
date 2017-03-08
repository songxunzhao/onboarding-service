package ee.tuleva.onboarding.mandate.content;

import ee.tuleva.domain.fund.Fund;
import ee.tuleva.onboarding.mandate.Mandate;
import ee.tuleva.onboarding.user.User;

import java.util.List;

public interface MandateContentCreator {

    List<MandateContentFile> getContentFiles(User user, Mandate mandate, List<Fund> funds);

}