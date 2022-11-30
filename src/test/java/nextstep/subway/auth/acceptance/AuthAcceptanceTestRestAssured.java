package nextstep.subway.auth.acceptance;

import io.restassured.RestAssured;
import nextstep.subway.auth.dto.TokenRequest;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.member.dto.MemberRequest;
import nextstep.subway.member.dto.MemberResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class AuthAcceptanceTestRestAssured {

    public static final String LOGIN = "/login/token";
    public static final String MEMBERS = "/members";
    public static final String MEMBERS_ME = "/members/me";

    static MemberResponse 내_정보_조회(TokenResponse tokenResponse) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(tokenResponse.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(MEMBERS_ME)
                .then().log().all()
                .extract().body().as(MemberResponse.class);
    }

    static TokenResponse 로그인_요청(MemberRequest memberRequest) {
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new TokenRequest(memberRequest.getEmail(), memberRequest.getPassword()))
                .when()
                .post(LOGIN)
                .then().log().all()
                .extract().body().as(TokenResponse.class);

    }
    static void 회원_등록되어_있음(MemberRequest memberRequest) {
        RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(memberRequest)
                .when()
                .post(MEMBERS)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }
}