package ee.tuleva.onboarding.comparisons.fundvalue.persistence;

import ee.tuleva.onboarding.comparisons.fundvalue.FundValue;
import ee.tuleva.onboarding.comparisons.fundvalue.FundValueProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class JdbcFundValueRepository implements FundValueRepository, FundValueProvider {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String FIND_LAST_VALUE_QUERY = "" +
            "SELECT * " +
            "FROM index_values " +
            "WHERE key=:fund " +
            "ORDER BY date DESC NULLS LAST " +
            "LIMIT 1";

    private static final String FIND_LATEST_VALUE_QUERY = "" +
            "SELECT * " +
            "FROM index_values " +
            "WHERE key=:fund AND date <= :date " +
            "ORDER BY date DESC " +
            "LIMIT 1";

    private static final String INSERT_VALUES_QUERY = "" +
            "INSERT INTO index_values (key, date, value) " +
            "VALUES (:fund, :time, :value)";

    private class FundValueRowMapper implements RowMapper<FundValue> {
        @Override
        public FundValue mapRow(ResultSet rs, int rowNum) throws SQLException {
            return FundValue.builder()
                    .comparisonFund(rs.getString("key"))
                    .time(rs.getTimestamp("date").toInstant())
                    .value(rs.getBigDecimal("value"))
                    .build();
        }
    }

    @Override
    public void saveAll(List<FundValue> fundValues) {
        List<Map<String, Object>> batchValues = fundValues
                .stream()
                .map(fundValue -> new MapSqlParameterSource()
                        .addValue("fund", fundValue.getComparisonFund(), Types.VARCHAR)
                        .addValue("time", Timestamp.from(fundValue.getTime()), Types.TIMESTAMP)
                        .addValue("value", fundValue.getValue(), Types.NUMERIC)
                        .getValues())
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(
                INSERT_VALUES_QUERY,
                batchValues.toArray(new Map[fundValues.size()])
        );
    }

    @Override
    public Optional<FundValue> findLastValueForFund(String fund) {
        List<FundValue> result = jdbcTemplate.query(
            FIND_LAST_VALUE_QUERY,
            new MapSqlParameterSource()
                    .addValue("fund", fund, Types.VARCHAR),
            new FundValueRowMapper()
        );
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<FundValue> getFundValueClosestToTime(String fund, Instant time) {
        List<FundValue> result = jdbcTemplate.query(
            FIND_LATEST_VALUE_QUERY,
                new MapSqlParameterSource()
                        .addValue("fund", fund, Types.VARCHAR)
                        .addValue("date", Timestamp.from(time), Types.TIMESTAMP),
                new FundValueRowMapper()
        );
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
