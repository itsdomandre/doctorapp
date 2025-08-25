package api;

import base.BaseUiTest;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticationApiTest extends BaseUiTest {

    private APIRequestContext api;

    @BeforeEach
    void setUpApi() {
        api = playwright.request().newContext(new APIRequest.NewContextOptions().setBaseURL(BE_URL));
    }

    @AfterEach
    void tearDownApi() {
        if (api != null) api.dispose();
    }

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

    private RegisterRequestPayload buildValidPayload(String email) {
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
    void register_whenEmailNotExists_shouldReturn200_andDTO() {
        String email = uniqueEmail();

        APIResponse res = api.post("/api/auth/register",
                RequestOptions.create().setData((buildValidPayload(email))));
        assertEquals(200, res.status(), res.statusText());

        String body = res.text();
        assertTrue(body.contains("\"email\":\"" + email + "\""));
        assertTrue(body.contains("\"fullName\":\"Malaquias Nistelrooy\""));
        assertTrue(body.contains("\"phoneNumber\":\"11991238863"));
        assertTrue(body.contains("\"role\":\"USER"));
    }

    @Test
    void register_whenEmailAlreadyExists_shouldReturn409() {
        String email = uniqueEmail();

        APIResponse ok = api.post("/api/auth/register",
                RequestOptions.create().setData((buildValidPayload(email))));
        assertEquals(200, ok.status());

        APIResponse conflict = api.post("/api/auth/register",
                RequestOptions.create().setData((buildValidPayload(email))));
        assertEquals(409, conflict.status());
        assertEquals("User Already Exists.", conflict.text());
    }

    @Test
    void register_withInvalidEmail_shouldReturn400_withErrorsList() {
        RegisterRequestPayload invalid = buildValidPayload("um-email-invalido.example.com");
        APIResponse res = api.post("/api/auth/register",
                RequestOptions.create().setData(invalid));

        assertEquals(400, res.status());

        String body = res.text();
        // GHE para MethodArgumentNotValidException -> { "errors": [ ... ] }
        assertTrue(body.contains("\"errors\""), "Esperava lista 'errors' do GlobalExceptionHandler");
        assertTrue(body.toLowerCase().contains("email"), "Mensagem deve mencionar e-mail inválido");
    }

    @Test
    void register_withMalformedJson_shouldReturn400_withErrorMessage() {
        // JSON malformado (chave sem aspas) para acionar HttpMessageNotReadableException
        String malformed = "{firstName:\"A\"}";

        APIResponse res = api.post("/api/auth/register",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(malformed));

        assertEquals(400, res.status());

        String body = res.text();
        // Mensagem configurada no teu GHE:
        assertTrue(body.contains("Invalid request format: verify the fields"),
                "Esperava mensagem global de JSON inválido");
    }
}
