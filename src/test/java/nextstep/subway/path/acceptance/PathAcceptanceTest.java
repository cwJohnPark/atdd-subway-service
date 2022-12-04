package nextstep.subway.path.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.member.dto.MemberRequest;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static nextstep.subway.auth.acceptance.AuthAcceptanceTestStep.로그인_됨;
import static nextstep.subway.auth.acceptance.AuthAcceptanceTestStep.로그인_요청;
import static nextstep.subway.line.acceptance.LineAcceptanceTest.지하철_노선_등록되어_있음;
import static nextstep.subway.line.acceptance.LineSectionAcceptanceIntegrationTest.지하철역_등록되어_있음;
import static nextstep.subway.line.acceptance.LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청;
import static nextstep.subway.member.MemberAcceptanceStep.회원_생성을_요청;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 경로 조회")
class PathAcceptanceTest extends AcceptanceTest {

    private StationResponse 교대역;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 남부터미널역;
    private LineResponse 삼호선;
    private TokenResponse 로그인_토큰;

    /**
     * <교대역>  ---   *2호선*(5m)   ---   강남역
     * |                                 |
     * *3호선* (5m)                    *신분당선* (1m)
     * |                                 |
     * 남부터미널역   --- *3호선*(5m) ---   <양재역>
     *
     * 요금
     * - 2호선: 200원
     * - 3호선: 300원
     * - 신분당선: 500원
     *
     * 연령
     * - 어린이
     */
    @BeforeEach
    void setup() {
        지하철역_등록();
        지하철_노선_등록();
        지하철_구간_등록되어_있음();
        로그인되어_있음();
    }

    /**
     * Feature: 지하철 구간 관련 기능
     *
     *   Background
     *     Given 지하철역 등록되어 있음
     *     And 지하철 노선 등록되어 있음
     *     And 지하철 구간 등록되어 있음
     *     And 로그인되어 있음
     *   Scenario: 두 역의 최단 거리 경로를 조회
     *     When 출발역에서 도착역까지의 최단 거리 경로 조회한다
     *     Then 최단 경로의 역 목록을 순서대로 알 수 있다
     *     And 최단 경로의 총 이동 거리를 알 수 있다
     *     And 지하철 이용 요금을 알 수 있다
     */
    @Test
    @DisplayName("지하철 최단 경로 조회")
    void findShortestPath() {
        PathResponse 경로_응답 = 지하철의_최단_경로_조회(교대역, 양재역);
        역_목록_조회됨(경로_응답);
        총_이동거리_조회됨(경로_응답);

        이용_요금_조회됨(경로_응답);
    }

    @Test
    @DisplayName("출발역과 도착역이 같은 경우 경로 조회를 할 수 없다")
    void sourceAndTargetEqual() {
        지하철의_최단경로_조회할_수_없음(교대역, 교대역);
    }

    @Test
    @DisplayName("출발역과 도착역이 연결되어 있지 않은 경우 경로 조회를 할 수 없다")
    void sourceAndTargetNotConnected() {
        StationResponse 광화문역 = 지하철역_등록되어_있음("광화문역");
        StationResponse 공덕역 = 지하철역_등록되어_있음("공덕역");

        지하철_노선_등록되어_있음(new LineRequest("5호선", "blue", 공덕역.getId(), 광화문역.getId(), 1));

        지하철의_최단경로_조회할_수_없음(교대역, 광화문역);
    }

    @Test
    @DisplayName("존재하지 않는 출발역일 경우 경로 조회를 할 수 없다")
    void sourceStationNotExists() {
        StationResponse 광화문역 = 지하철역_등록되어_있음("광화문역");

        지하철의_최단경로_조회할_수_없음(광화문역, 교대역);
    }

    @Test
    @DisplayName("존재하지 않는 도착역일 경우 경로 조회를 할 수 없다")
    void targetStationNotExists() {
        StationResponse 광화문역 = 지하철역_등록되어_있음("광화문역");
        지하철의_최단경로_조회할_수_없음(교대역, 광화문역);
    }

    private void 로그인되어_있음() {
        MemberRequest memberRequest = new MemberRequest("email@mail.com", "1234", 10);
        회원_생성을_요청(memberRequest);
        ExtractableResponse<Response> 로그인_응답 = 로그인_요청(memberRequest);
        로그인_토큰 = 로그인_됨(로그인_응답);
    }

    private void 이용_요금_조회됨(PathResponse response) {
        int 노선_요금 = 500;
        int 이동_거리_요금 = 1_250;
        int 할인_적용_금액 = (int) Math.floor((노선_요금 + 이동_거리_요금 - 350) * 0.5);
        PathAcceptanceStep.이용_요금_조회됨(response, 할인_적용_금액);
    }

    private void 지하철의_최단경로_조회할_수_없음(StationResponse source, StationResponse target) {
        ExtractableResponse<Response> response = PathAcceptanceStep.get(source, target);
        PathAcceptanceStep.expectBadRequest(response);
    }

    private void 총_이동거리_조회됨(PathResponse response) {
        assertThat(response.getDistance())
                .isEqualTo(6);
    }

    private void 역_목록_조회됨(PathResponse response) {
        assertThat(response.getStations().stream().map(StationResponse::getName))
                .containsExactly(교대역.getName(), 강남역.getName(), 양재역.getName());
    }

    private PathResponse 지하철의_최단_경로_조회(StationResponse source, StationResponse target) {
        return PathAcceptanceStep.get(source, target, 로그인_토큰).body()
                .as(PathResponse.class);
    }

    private void 지하철_구간_등록되어_있음() {
        지하철_노선에_지하철역_등록_요청(삼호선, 남부터미널역, 양재역, 5);
    }

    private void 지하철_노선_등록() {
        지하철_노선_등록되어_있음(
                new LineRequest("이호선", "blue", 교대역.getId(), 강남역.getId(),5, 200))
                .body().as(LineResponse.class);
        지하철_노선_등록되어_있음(
                new LineRequest("신분당선", "blue", 강남역.getId(), 양재역.getId(), 1, 500))
                .body().as(LineResponse.class);
        삼호선 = 지하철_노선_등록되어_있음(
                new LineRequest("삼호선", "blue", 교대역.getId(), 남부터미널역.getId(), 10, 300))
                .body().as(LineResponse.class);
    }

    private void 지하철역_등록() {
        양재역 = 지하철역_등록되어_있음("양재역");
        강남역 = 지하철역_등록되어_있음("강남역");
        남부터미널역 = 지하철역_등록되어_있음("남부터미널역");
        교대역 = 지하철역_등록되어_있음("교대역");
    }

}
