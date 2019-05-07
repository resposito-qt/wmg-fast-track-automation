package framework.platform.html.support;

import framework.adapters.WebDriverManager;
import framework.platform.ConfigProvider;
import framework.platform.html.SelectorType;
import framework.platform.html.WebObject;
import javafx.util.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utilitarian class which serves as search engine of HTML elements for {@link WebObject}.
 * <p>
 * Contains methods for identification of locators and search of elements.
 */
public abstract class HtmlElementUtils {

    private static RemoteWebDriver driver = WebDriverManager.getDriver();

    /**
     * Parses locator string to identify the proper By subclass before calling Selenium
     * {@link WebElement#findElement(By)} to locate the web element.
     *
     * @param locator - A String that represents the means to locate this element (could be id/name/xpath/css locator).
     * @return - A {@link RemoteWebElement} that represents the html element that was located using the locator
     * provided.
     */
    public static WebElement locateElement(Pair<SelectorType, String> locator, int implicitTimeoutSeconds) {
        By locatorBy = getFindElementType(locator);
        boolean areWaitsEquals = ConfigProvider.IMPLICIT_WAIT_SECONDS == implicitTimeoutSeconds;
        if (!areWaitsEquals) {
            driver.manage().timeouts().implicitlyWait(implicitTimeoutSeconds, TimeUnit.SECONDS);
        }
        WebElement webElement = driver.findElement(locatorBy);
        if (!areWaitsEquals) {
            driver.manage().timeouts().implicitlyWait(ConfigProvider.IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS);
        }
        return webElement;
    }

    /**
     * Parses locator string to identify the proper By subclass before calling Selenium
     * {@link WebElement#findElements(By)} to locate the web elements.
     *
     * @param locator - A String that represents the means to locate this element (could be id/name/xpath/css locator).
     * @return A {@link WebElement} list that represents the html elements that was located using the locator provided.
     */
    public static List<WebElement> locateElements(Pair<SelectorType, String> locator) {
        By locatorBy = getFindElementType(locator);
        List<WebElement> webElementsFound = driver.findElements(locatorBy);
        if (webElementsFound.isEmpty()) {
            throw new NoSuchElementException(generateUnsupportedLocatorMsg(locator.getValue()));
        }
        return webElementsFound;
    }

    /**
     * Detects Selenium {@link org.openqa.selenium.By By} type depending on what the locator string starts with.
     *
     * @param locator - A String that represents the means to locate this element (could be id/name/xpath/css locator).
     * @return The {@link By} sub-class that represents the actual location strategy that will be used.
     */
    public static By getFindElementType(Pair<SelectorType, String> locator) {
        String locatorValue = locator.getValue();
        String locatorType = locator.getKey().getValue();
        By valueToReturn;
        if (locatorType.equals("xpath")) {
            valueToReturn = By.xpath(locatorValue);
        } else {
            valueToReturn = By.cssSelector(locatorValue);
        }
        return valueToReturn;
    }

    /**
     * This method generates error message for unsupported locator.
     */
    private static String generateUnsupportedLocatorMsg(String locator) {
        return "Unsupported locator {" + locator
                + "}. Locator has to be either a name, id, link text, xpath, or css selector.";
    }
}
