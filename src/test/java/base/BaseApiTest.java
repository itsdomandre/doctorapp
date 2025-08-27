package base;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseApiTest {
    protected static final String BE_URL = System.getProperty("BE_URL", "http://localhost:8080");

    @BeforeAll
    static void beforeAll() {
        RestAssured.baseURI = BE_URL + "/api";
    }

    @AfterAll
    static void afterAll() {
        RestAssured.reset();
    }

}
