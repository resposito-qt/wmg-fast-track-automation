package framework.platform.utilities;

import framework.adapters.WebDriverManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * Contains methods for handling specific notifications on mobile devices etc.
 */
public class MobileUtils {

    private static final String CLOSE_BUTTON = "infobar_close_button";

    public static void closeAllNotifications(){
        AppiumDriver appiumDriver = (AppiumDriver) WebDriverManager.getDriver();
        appiumDriver.context("NATIVE_APP");
        for (int elementNumber = 0; elementNumber <= appiumDriver.findElements(By.id(CLOSE_BUTTON)).size(); elementNumber++) {
            appiumDriver.findElement(By.id(CLOSE_BUTTON)).click();
        }
        appiumDriver.context("WEBVIEW_1");
    }

}
