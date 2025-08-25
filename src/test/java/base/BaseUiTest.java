package base;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseUiTest {
    protected static Playwright playwright;
    protected static Browser browser;

    protected static final String FE_URL = System.getProperty("FE_URL", "http://localhost:5173");
    protected static final String BE_URL = System.getProperty("BE_URL", "http://localhost:8080");

    @BeforeAll
    static void globalSetup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                //.setHeadless(true));
                .setHeadless(false)
                .setSlowMo(150));
    }

    @AfterAll
    static void globalTeardown() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    protected APIRequestContext newApiContext() {
        return playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(BE_URL));
    }
}
