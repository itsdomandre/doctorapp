package ui;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NewAppointmentUiTest {
    protected static final String FE_URL = System.getProperty("FE_URL", "http://localhost:5173");
    static Playwright playwright;
    static Browser browser;
    BrowserContext browserContext;
    Page page;

    @BeforeAll
    static void setupClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false));
    }

    @AfterAll
    static void tearDownClass() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setup() {
        browserContext = browser.newContext();
        page = browserContext.newPage();
    }

    @AfterEach
    void tearDown() {
        browserContext.close();
    }

    @Test
    void shouldCreatedNewAppointment() {

        page.navigate(FE_URL + "/login");

        page.locator("input[name='email']").fill("r9@example.com");
        page.locator("input[name='password']").fill("123456");
        page.click("button[type=submit]");
        page.waitForURL("**/app");

        page.navigate(FE_URL + "/app/appointments/new");
        page.getByLabel("Data").fill("2025-10-09");

        page.getByLabel("Horário").selectOption(new SelectOption().setIndex(2));

        page.getByLabel("Procedimento").selectOption(new SelectOption().setIndex(3));
        page.getByLabel("Observações (opcional)").fill("Auto Test Playwright, ok!");
        page.click("button[type=submit]");

        page.waitForSelector("text=Pedido enviado!");
        assertTrue(page.getByText("Pedido enviado!").isVisible());
    }

    @Test
    void shouldShowValidationErrorWhenRequiredFieldsMissing() {
        page.navigate(FE_URL + "/login");

        page.locator("input[name='email']").fill("r9@example.com");
        page.locator("input[name='password']").fill("123456");

        page.click("button[type=submit]");
        page.waitForURL("**/app");

        page.navigate(FE_URL + "/app/appointments/new");
        page.click("button[type=submit]");

        Assertions.assertTrue(page.locator("select:invalid").first().isVisible(),
                "Algum campo está marcado como inválido por supostamente nao ter sido preenchido");
    }

    @Test
    void shouldShowNativeValidationErrorWhenSlotNotSelected() {
        page.navigate(FE_URL + "/login");

        page.locator("input[name='email']").fill("r9@example.com");
        page.locator("input[name='password']").fill("123456");
        page.click("button[type=submit]");
        page.waitForURL("**/app");

        page.navigate(FE_URL + "/app/appointments/new");

        page.getByLabel("Data").fill("2025-09-09");
        page.getByLabel("Procedimento").selectOption(new SelectOption().setIndex(3));
        page.getByLabel("Observações (opcional)").fill("Auto Test Playwright, ok!");
        page.click("button[type=submit]");

        Assertions.assertTrue(
                page.locator("select:invalid").first().isVisible(),
                "O campo Horário não está preenchido"
        );
    }
}
