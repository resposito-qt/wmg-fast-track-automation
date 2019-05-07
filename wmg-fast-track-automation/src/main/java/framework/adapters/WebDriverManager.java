package framework.adapters;

import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Provides instantiation and access to instance of webdriver.
 */
public class WebDriverManager {

    public static RemoteWebDriver getDriver() {
        return UIAutomationTestListener.getDriver();
    }
}
