package ee.tuleva.onboarding.auth;

import ee.tuleva.onboarding.auth.command.AuthenticateCommand;
import ee.tuleva.onboarding.auth.idcard.IdCardAuthService;
import ee.tuleva.onboarding.auth.mobileid.MobileIDSession;
import ee.tuleva.onboarding.auth.mobileid.MobileIdAuthService;
import ee.tuleva.onboarding.auth.response.AuthenticateResponse;
import ee.tuleva.onboarding.auth.response.IdCardLoginResponse;
import ee.tuleva.onboarding.auth.session.GenericSessionStore;
import ee.tuleva.onboarding.auth.smartid.SmartIdAuthService;
import ee.tuleva.onboarding.auth.smartid.SmartIdSession;
import ee.tuleva.onboarding.error.ValidationErrorsException;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URLDecoder;

import static ee.tuleva.onboarding.auth.command.AuthenticationType.SMART_ID;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {

  private final MobileIdAuthService mobileIdAuthService;
  private final SmartIdAuthService smartIdAuthService;
  private final GenericSessionStore genericSessionStore;
  private final IdCardAuthService idCardAuthService;

  @Value("${frontend.url}")
  private String frontendUrl;

  @ApiOperation(value = "Initiate authentication")
  @RequestMapping(
      method = POST,
      value = "/authenticate",
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<AuthenticateResponse> authenticate(
      @Valid @RequestBody AuthenticateCommand authenticateCommand, @ApiIgnore Errors errors) {

    if (errors != null && errors.hasErrors()) {
      throw new ValidationErrorsException(errors);
    }

    if (SMART_ID == authenticateCommand.getType()) {
      SmartIdSession loginSession = smartIdAuthService.startLogin(authenticateCommand.getPersonalCode());
      genericSessionStore.save(loginSession);
      return new ResponseEntity<>(AuthenticateResponse.fromSmartIdSession(loginSession), HttpStatus.OK);
    }

    MobileIDSession loginSession =
        mobileIdAuthService.startLogin(authenticateCommand.getPhoneNumber(), authenticateCommand.getPersonalCode());
    genericSessionStore.save(loginSession);
    return new ResponseEntity<>(AuthenticateResponse.fromMobileIdSession(loginSession), HttpStatus.OK);
  }

  @SneakyThrows
  @ApiOperation(value = "ID card login")
  @RequestMapping(
      method = {GET, POST},
      value = "/idLogin")
  @ResponseBody
  public IdCardLoginResponse idLogin(
      @RequestHeader(value = "ssl-client-verify") String clientCertificateVerification,
      @RequestHeader(value = "ssl-client-cert") String clientCertificate,
      @ApiIgnore HttpServletResponse response,
      @ApiIgnore HttpMethod httpMethod) {
    if (!"SUCCESS".equals(clientCertificateVerification)) {
      throw new UnauthorizedClientException("Client certificate not verified");
    }

    idCardAuthService.checkCertificate(URLDecoder.decode(clientCertificate, "UTF-8"));

    if (httpMethod.equals(HttpMethod.GET)) {
      response.sendRedirect(frontendUrl + "?login=idCard");
    }
    return IdCardLoginResponse.success();
  }
}
