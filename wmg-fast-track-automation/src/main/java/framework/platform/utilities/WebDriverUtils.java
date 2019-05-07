package framework.platform.utilities;

import framework.Logger;
import framework.adapters.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.interactions.Actions;

/**
 * Utilitarian class which contains various methods for executing JavaScript's and performing custom {@link Actions}. TODO
 */
public class WebDriverUtils {

    /**
     * Method which will execute given javascript.
     */
    public static String executeJS(String jsScript) {
        JavascriptExecutor js = (JavascriptExecutor) WebDriverManager.getDriver();
        return js.executeScript(jsScript).toString();
    }

    /**
     * TODO Clarification.
     */
    public static void scrollPage(Integer y) {
        executeJS("scroll(0, " + y.toString() + ");");
    }

    /**
     * Checks whether source code of the current web page contains given text.
     */
    public static boolean isPageSourceContains(String value) {
        Logger.info("Verify " + value + " value exists on page: " + WebDriverManager.getDriver().getCurrentUrl());
        return WebDriverManager.getDriver().getPageSource().toLowerCase().contains(value.toLowerCase());
    }

    /**
     * Checks whether current URL ends with given text or not.
     */
    public static boolean currentUrlEndsWith(String urlPart) {
        return WebDriverManager.getDriver().getCurrentUrl().endsWith(urlPart);
    }

    /**
     * Checks whether current URL ends with given text or not.
     */
    public static boolean currentUrlContains(String urlPart) {
        return WebDriverManager.getDriver().getCurrentUrl().contains(urlPart);
    }

    public static String getCurrentUrl() {
        return WebDriverManager.getDriver().getCurrentUrl();
    }

    public static void confirmAlert() {
        WebDriverManager.getDriver().switchTo().alert().accept();
    }

    public static boolean isAlertPresent() {
        try {
            WebDriverManager.getDriver().switchTo().alert();
            return true;
        } catch (NoAlertPresentException Ex) {
            return false;
        }
    }
}
