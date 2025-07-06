package e2e;


import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

public class LoginTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext browserContext;
    Page page;

    @BeforeAll
    static void setupClass(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false));
    }

    @AfterAll
    static void tearDownClass(){
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setup(){
        browserContext = browser.newContext();
        page = browserContext.newPage();
    }

    @AfterEach
    void tearDown(){
        browserContext.close();
    }

    @Test
    void shouldLoginSuccessfully(){
        page.navigate("http://192.168.1.108:5173/login");

        page.fill("input[Placeholder=Email]" , "didier@example.com");
        page.fill("input[Placeholder=Senha]" , "1234");
        page.click("button[type=submit]");

        page.waitForURL("**/home");
        Assertions.assertTrue(page.url().contains("/home"));

        Assertions.assertTrue(page.getByText("Bem-vindo").isVisible());
    }

}
