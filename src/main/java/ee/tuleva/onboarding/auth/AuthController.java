package ee.tuleva.onboarding.auth;

import com.codeborne.security.mobileid.MobileIDSession;
import ee.tuleva.onboarding.auth.command.AuthenticateCommand;
import ee.tuleva.onboarding.auth.idcard.IdCardAuthService;
import ee.tuleva.onboarding.auth.mobileid.MobileIdAuthService;
import ee.tuleva.onboarding.auth.mobileid.MobileIdSessionStore;
import ee.tuleva.onboarding.auth.response.AuthenticateResponse;
import io.swagger.annotations.ApiOperation;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final MobileIdAuthService mobileIdAuthService;
    private final MobileIdSessionStore mobileIdSessionStore;
    private final IdCardAuthService idCardAuthService;

    @ApiOperation(value = "Initiate authentication")
    @RequestMapping(
            method = POST,
            value = "/authenticate",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AuthenticateResponse> authenticate(@Valid @RequestBody AuthenticateCommand authenticateCommand) {
        MobileIDSession loginSession = mobileIdAuthService.startLogin(authenticateCommand.getPhoneNumber());
        mobileIdSessionStore.save(loginSession);
        return new ResponseEntity<>(AuthenticateResponse.fromMobileIdSession(loginSession), HttpStatus.OK);
    }

    @ApiOperation(value = "ID card login")
    @RequestMapping(method = POST, value = "/idLogin")
    @ResponseBody
    public IdLoginResponse idLogin(@RequestHeader(value="ssl_client_verify") String clientCertificateVerification,
                          @RequestHeader(value="ssl_client_cert") String clientCertificate) {
        if(!"SUCCESS".equals(clientCertificateVerification)) {
            throw new UnauthorizedClientException("Client certificate not verified");
        }
        idCardAuthService.checkCertificate(clientCertificate);

        return IdLoginResponse.builder()
                .success(true)
                .build();
    }

    @Data
    @Builder
    private static class IdLoginResponse {
        private boolean success;
    }

}
