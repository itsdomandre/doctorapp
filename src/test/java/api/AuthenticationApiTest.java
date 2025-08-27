package api;

import base.BaseApiTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.then;

public class AuthenticationApiTest extends BaseApiTest {
    private final ObjectMapper om = new ObjectMapper();

    private String uniqueEmail() {
        return "malaquias" + UUID.randomUUID().toString().substring(0, 6) + "@example.com"; // ficou dessa maneira para não dar conflito na sequencia dos testes
    }

    static class RegisterRequestPayload {
        public String firstName;
        public String lastName;
        public String email;
        public String password;
        public String phoneNumber;
        public String birthdate;
        public String role;
    }

    private RegisterRequestPayload validPayload(String email) {
        RegisterRequestPayload p = new RegisterRequestPayload();
        p.firstName = "Malaquias";
        p.lastName = "Nistelrooy";
        p.email = email;
        p.password = "1234";
        p.phoneNumber = "11991238863";
        p.birthdate = "2001-10-08";
        p.role = "USER";
        return p;
    }

    @Test
    void register_whenEmailNotExists_shouldReturn200_andDTO() throws JsonProcessingException {
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(validPayload(email))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200)
                .body("email", equalTo(email))
                .body("fullName", equalTo("Malaquias Nistelrooy"))
                .body("phoneNumber", equalTo("11991238863"))
                .body("role", equalTo("USER"));
    }

    @Test
    void register_whenEmailAlreadyExists_shouldReturn409() {
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(validPayload(email))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .body(validPayload(email))

                .when()
                .post("/auth/register")

                .then()
                .statusCode(409)
                .body(anyOf(equalTo("User Already Exists.")));
    }

    @Test
    void register_withInvalidEmail_shouldReturn400_withMsgError() {
        String email = "exammple@googlecom";

        given()
                .contentType(ContentType.JSON)
                .body(validPayload(email))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(400)
                .body(containsString("Email must include a valid domain (Ex.: @gmail.com)"));
    }

    // TODO: Test
    @Test
    void register_whenPasswordDoesNotMeetRequirements_shouldReturn400 (){
        String email = uniqueEmail();

    }
}