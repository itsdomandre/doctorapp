package ui;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest {
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
    void shouldLoginSuccessfully() {
        page.navigate(FE_URL + "/login");

        page.locator("input[name='email']").fill("r9@example.com");
        page.locator("input[name='password']").fill("123456");

        page.click("button[type=submit]");
        page.waitForURL("**/app");

        assertTrue(page.url().contains("/app"), "User should be redirect to /app after login");
        assertTrue(page.getByText("DoctorApp").isVisible(), "should show 'DoctorApp' in top menu");
    }

    @Test
    void shouldLoginUnsuccessfullyShowBadCredentials() {
        page.navigate(FE_URL + "/login");

        page.locator("input[name='email']").fill("r10@example.com");
        page.locator("input[name='password']").fill("12345");
        page.click("button[type=submit]");

        assertTrue(page.url().contains("/login"), "User should be login screen");
        Locator errorMsg = page.getByText("Credenciais inválidas", new Page.GetByTextOptions().setExact(true));
        errorMsg.waitFor();
        assertTrue(errorMsg.isVisible(), "Credenciais Inválidas should be return in invalid login");
    }
}