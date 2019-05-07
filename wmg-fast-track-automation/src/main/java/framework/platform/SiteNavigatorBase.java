package framework.platform;

import framework.Logger;
import framework.adapters.WebDriverManager;
import framework.components.PageObject;
import org.openqa.selenium.support.PageFactory;
import pages.HomePage;
import pages.LoginPage;

/**
 * This class provides methods for initialization of page objects.
 * <p>
 * All custom site navigators must be derived from this class.
 */
public class SiteNavigatorBase {

    public static void openPage(String url) {
        String resolvedUrl = ConfigProvider.baseUrl + url;
        Logger.info("Navigating URL: " + resolvedUrl);
        WebDriverManager.getDriver().navigate().to(resolvedUrl);
    }

    protected static <T> T openPageWithInit(String page, Class<T> expectedPage) {
        openPage(page);
        return PageFactory.initElements(WebDriverManager.getDriver(), expectedPage);
    }

    public static LoginPage goToLoginPage() {
        return openPageWithInit("/logout", LoginPage.class);
    }

    public static void navigateTo(String url) {
        Logger.info("Navigating URL: " + url);
        WebDriverManager.getDriver().navigate().to(url);
        PageObject.waitForAjaxRequestToBeFinished();
    }

    public static HomePage goToHomePage() {
        return new HomePage();
    }
}
