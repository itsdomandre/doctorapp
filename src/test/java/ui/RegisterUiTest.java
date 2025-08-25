package ui;

import base.BaseUiTest;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterUiTest extends BaseUiTest {

    private BrowserContext ctx;
    private Page page;

    @BeforeEach
    void setup() {
        ctx = browser.newContext();
        page = ctx.newPage();
    }

    @AfterEach
    void teardown() {
        if (ctx != null) ctx.close();
    }

    private String uniqueEmail() {
        return "malaquias+" + UUID.randomUUID().toString().substring(0, 6) + "@example.com";
    }

    @Test
    void register_happyPath_shouldShowSuccessFeedback() {
        String email = uniqueEmail();
        page.navigate(FE_URL + "/register");

        page.locator("input[name='firstName']").fill("Malaquias");
        page.locator("input[name='lastName']").fill("Nistelrooy");
        page.locator("input[name='email']").fill(email);
        page.locator("input[name='password']").fill("123456");
        page.locator("input[name='confirmPassword']").fill("123456");
        page.locator("input[name='phoneNumber']").fill("11991238863");
        page.locator("input[name='birthdate']").fill("2001-10-08");

        Response resp = page.waitForResponse(
                r -> r.url().contains("/api/auth/register") && r.status() == 200,
                () -> page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Criar conta")).click()
        );
        assertEquals(200, resp.status(), "Registro deveria retornar 200");

        boolean success =
                page.getByText("Verifique seu e-mail").isVisible()
                        || page.getByText("Cadastro realizado").isVisible()
                        || page.getByText("Conta criada").isVisible();

        assertTrue(success, "Deveria mostrar feedback de sucesso pós-registro");
    }

    @Test
    void register_whenEmailAlreadyExists_shouldShowDuplicateError() {
        String email = uniqueEmail();

        // Pré-condição: cria usuário via API (o teste tem que ser "independente")
        APIRequestContext api = playwright.request()
                .newContext(new APIRequest.NewContextOptions().setBaseURL(BE_URL));
        try {
            String json = """
        {
          "firstName": "Malaquias",
          "lastName": "Nistelrooy",
          "email": "%s",
          "password": "123456",
          "phoneNumber": "11991238863",
          "birthdate": "2001-10-08",
          "role": "USER"
        }
        """.formatted(email);

            APIResponse pre = api.post("/api/auth/register",
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(json));

            assertEquals(200, pre.status(),
                    "Pré-condição falhou: não consegui criar usuário via API");
        } finally {
            api.dispose(); // fecha o contexto manualmente
        }

        page.navigate(FE_URL + "/register");

        page.locator("input[name='firstName']").fill("Malaquias");
        page.locator("input[name='lastName']").fill("Nistelrooy");
        page.locator("input[name='email']").fill(email);
        page.locator("input[name='password']").fill("123456");
        page.locator("input[name='confirmPassword']").fill("123456");
        page.locator("input[name='phoneNumber']").fill("11991238863");
        page.locator("input[name='birthdate']").fill("2001-10-08");

        Response resp = page.waitForResponse(
                r -> r.url().contains("/api/auth/register") && r.status() == 409,
                () -> page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Criar conta")).click()
        );
        assertEquals(409, resp.status(), "Deveria retornar 409 para email duplicado");

        Locator err = page.getByText("User Already Exists.", new Page.GetByTextOptions().setExact(true));
        err.waitFor(); // espera até renderizar o erro

        assertTrue(err.isVisible(), "Esperava que a UI mostrasse erro de e-mail inválido");
    }

    @Test
    void register_withInvalidEmail_shouldShowClientSideValidation() {
        page.navigate(FE_URL + "/register");

        page.locator("input[name='firstName']").fill("Malaquias");
        page.locator("input[name='lastName']").fill("Nistelrooy");
        page.locator("input[name='email']").fill("email-invalido.example.com");
        page.locator("input[name='password']").fill("123456");
        page.locator("input[name='confirmPassword']").fill("123456");
        page.locator("input[name='phoneNumber']").fill("11991238863");
        page.locator("input[name='birthdate']").fill("2001-10-08");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Criar conta")).click();

        boolean clientError =
                page.getByText("Endereço de e-mail inválido.").isVisible()
                        || page.getByText("Email must include a valid domain").isVisible()
                        || Boolean.TRUE.equals(page.getByPlaceholder("Email").isVisible()
                        && "true".equals(page.getByPlaceholder("Email").getAttribute("aria-invalid")));

        assertTrue(clientError, "Esperava que a UI mostrasse erro de e-mail inválido");
    }
}
