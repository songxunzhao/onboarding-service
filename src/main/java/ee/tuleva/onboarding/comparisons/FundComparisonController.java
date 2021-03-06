package ee.tuleva.onboarding.comparisons;

import ee.tuleva.onboarding.auth.principal.AuthenticatedPerson;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class FundComparisonController {

    private static final Instant DEFAULT_TIME = parseInstant("1900-01-01");

    private final FundComparisonCalculatorService fundComparisonCalculatorService;

    @ApiOperation(value = "Compare the current user's return to estonian and world average")
    @GetMapping("/fund-comparison")
    public FundComparison getComparison(
            @ApiIgnore @AuthenticationPrincipal AuthenticatedPerson authenticatedPerson,
            @RequestParam(value = "from", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date startDate,
            @RequestParam(value = "pillar", required = false, defaultValue = "2") Integer pillar) {
        Instant startTime = startDate == null ? DEFAULT_TIME : startDate.toInstant();
        return fundComparisonCalculatorService.calculateComparison(authenticatedPerson, startTime, pillar);
    }

    private static Instant parseInstant(String format) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(format).toInstant();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}

