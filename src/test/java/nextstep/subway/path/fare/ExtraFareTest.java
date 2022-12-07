package nextstep.subway.path.fare;

import nextstep.subway.line.domain.Distance;
import nextstep.subway.path.domain.Path;
import nextstep.subway.path.fare.Fare;
import nextstep.subway.path.fare.extra.BasicExtraFareStrategy;
import nextstep.subway.path.fare.extra.ExtraFare;
import org.assertj.core.util.Lists;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ExtraFareTest {

    @ParameterizedTest
    @ValueSource(ints = {1,2,9,10})
    void testCalculateBasicFare(int distance) {
        Path path = new Path(Lists.emptyList(), Collections.emptyList(), Distance.valueOf(distance));

        Fare fare = ExtraFare.calculateExtraFare(path);

        assertThat(fare).isEqualTo(BasicExtraFareStrategy.BASIC_FARE);
    }

    @ParameterizedTest
    @CsvSource({"10,1250","14,1250","15,1350","49,1950","50,2050"})
    void testUntilFiftyKiloFarePolicy(int distance, int fareAmount) {
        Path path = new Path(Lists.emptyList(), Collections.emptyList(), Distance.valueOf(distance));

        Fare fare = ExtraFare.calculateExtraFare(path);

        assertThat(fare).isEqualTo(Fare.valueOf(fareAmount));
    }

    @ParameterizedTest
    @CsvSource({"57,2050", "58,2150", "66,2250", "106,2750"})
    void testAboveFiftyKiloFarePolicy(int distance, int fareAmount) {
        Path path = new Path(Lists.emptyList(), Collections.emptyList(), Distance.valueOf(distance));

        Fare fare = ExtraFare.calculateExtraFare(path);

        assertThat(fare).isEqualTo(Fare.valueOf(fareAmount));
    }
}