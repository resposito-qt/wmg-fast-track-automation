package framework.platform.html;

import framework.Logger;
import framework.Settings;
import framework.adapters.WebDriverManager;
import framework.components.PageObject;
import framework.platform.ConfigProvider;
import framework.platform.html.support.HtmlElementUtils;
import framework.platform.utilities.WebDriverUtils;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * One of the framework core classes.
 * <p>
 * Object which is used to work with abstract HTML elements.
 */
public class WebObject extends By {
    private Pair<SelectorType, String> selectorPair;
    private String elementName;
    private WebElement webElement;
    private String selectorString;
    private static RemoteWebDriver driver = WebDriverManager.getDriver();

    private WebElement getElement(int implicitWait) {
        if (this.webElement == null) {
            try {
                selectorPair = PageObject.resolveSelector(selectorString);
                webElement = HtmlElementUtils.locateElement(selectorPair, implicitWait);
            } catch (NoSuchElementException n) {
                addInfoForNoSuchElementException(n);
            }
        }
        return webElement;
    }

    private WebElement getElement() {
        return getElement(ConfigProvider.IMPLICIT_WAIT_SECONDS);
    }

    /**
     * A utility method to provide additional information to the user when a NoSuchElementException is thrown.
     *
     * @param cause The associated cause for the exception.
     */
    private void addInfoForNoSuchElementException(NoSuchElementException cause) {
        StringBuilder msg = new StringBuilder("Unable to find webElement ");
        if (this.elementName != null) {
            msg.append(this.elementName).append(" on ");
        }
        msg.append(" using the selectorPair {").append(selectorPair).append("}");
        throw new NoSuchElementException(msg.toString(), cause);
    }

    public WebObject(Pair<SelectorType, String> selectorPair, WebElement webElement) {
        this.selectorPair = selectorPair;
        this.webElement = webElement;
    }

    /**
     * Used for elements init with @Locator annotation.
     */
    public WebObject(String selectorString, String elementName) {
        this.selectorString = selectorString;
        this.elementName = elementName;
    }

    /**
     * Finds element on the page and returns the visible (i.e. not hidden by CSS) innerText of this element, including
     * sub-elements, without any leading or trailing whitespace.
     *
     * @return The innerText of this element.
     */
    public String getText() {
        return getElement().getText();
    }

    /**
     * Checks if element is present in the html dom. An element that is present in the html dom does not mean it is
     * visible.
     *
     * @return True if element is present, false otherwise.
     */
    public boolean isPresent() {
        boolean returnValue = false;
        try {
            if (getElement() != null) {
                returnValue = true;
            }
        } catch (NoSuchElementException e) {
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Is this element displayed or not? This method avoids the problem of having to parse an element's "style"
     * attribute.
     *
     * @return Whether or not the element is displayed
     */
    public boolean isVisible() {
        webElement = getElement();
        try {
            return webElement != null && webElement.isDisplayed();
        } catch (ElementNotVisibleException var1) {
            return false;
        } catch (NoSuchElementException var2) {
            return false;
        } catch (StaleElementReferenceException var3) {
            return false;
        }
    }

    public boolean isCurrentlyVisible() {
        webElement = getElement(0);
        try {
            return webElement != null && webElement.isDisplayed();
        } catch (ElementNotVisibleException var1) {
            return false;
        } catch (NoSuchElementException var2) {
            return false;
        } catch (StaleElementReferenceException var3) {
            return false;
        }
    }

    /**
     * Is the element currently enabled or not? This will generally return true for everything but disabled input
     * elements.
     *
     * @return True if element is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return getElement().isEnabled();
    }

    /**
     * Is the element currently selected or not?
     *
     * @return True if element is selected, false otherwise.
     */
    public boolean isSelected() {
        return getElement().isSelected();
    }

    /**
     * This method will wait until element will be visible.
     */
    public WebObject waitUntilVisible() {
        Logger.debug("Waiting for element " + elementName);
        try {
            waitForCondition().until(driver -> this.isVisible());
        } catch (Throwable var2) {
            Logger.err(">>>>> ELEMENT " + elementName + " is not found <<<<<<");
            this.throwErrorWithCauseIfPresent(var2, var2.getMessage());
        }
        return this;
    }

    /**
     * This method will wait until element will be enabled.
     */
    public WebObject waitUntilEnabled() {
        try {
            this.waitUntilVisible();
            this.waitForCondition().until(driver -> this.isEnabled());
        } catch (Throwable var2) {
            this.throwErrorWithCauseIfPresent(var2, var2.getMessage());
        }
        return this;
    }

    /**
     * This method will return instance of {@link FluentWait} which can be used to wait until specific condition.
     */
    private WebDriverWait waitForCondition() {
        return new WebDriverWait(driver, ConfigProvider.IMPLICIT_WAIT_SECONDS);
    }

    /**
     * This method will select option with given value from select list.
     */
    public void selectByValue(String value) {
        new Select(getElement()).selectByValue(value);
    }

    /**
     * This method will select option which contains given text from select list.
     */
    public void selectByText(String text) {
        waitUntilEnabled();
        Select dropdown = new Select(getElement());
        dropdown.selectByVisibleText(text);
    }

    /**
     * This element will return text of selected option from the select list.
     */
    public String getSelectedOption() {
        waitUntilEnabled();
        Select dropdown = new Select(getElement());
        return dropdown.getFirstSelectedOption().getText();
    }

    public List<WebElement> getOptions() {
        waitUntilEnabled();
        Select dropdown = new Select(getElement());
        return dropdown.getOptions();
    }

    /**
     * This method will switch focus to the frame which is represented by {@link framework.platform.html.WebObject}
     */
    public WebObject switchToFrame() {
        driver.switchTo().frame(getElement());
        return this;
    }

    /**
     * Get the value of a the given attribute of the element. Will return the current value, even if this has been
     * modified after the page has been loaded. More exactly, this method will return the value of the given attribute,
     * unless that attribute is not present, in which case the value of the property with the same name is returned. If
     * neither value is set, null is returned. The "style" attribute is converted as best can be to a text
     * representation with a trailing semi-colon. The following are deemed to be "boolean" attributes, and will return
     * either "true" or null: async, autofocus, autoplay, checked, compact, complete, controls, declare, defaultchecked,
     * defaultselected, defer, disabled, draggable, ended, formnovalidate, hidden, indeterminate, iscontenteditable,
     * ismap, itemscope, loop, multiple, muted, nohref, noresize, noshade, novalidate, nowrap, open, paused, pubdate,
     * readonly, required, reversed, scoped, seamless, seeking, selected, spellcheck, truespeed, willvalidate. Finally,
     * the following commonly mis-capitalized attribute/property names are evaluated as expected: class, readonly
     *
     * @param attributeName the attribute name to get current value
     * @return The attribute's current value or null if the value is not set.
     */
    public String getAttribute(String attributeName) {
        return getElement().getAttribute(attributeName);
    }

    /**
     * Gets the (whitespace-trimmed) value of an input field (or anything else with a value parameter). For
     * checkbox/radio elements, the value will be "on" or "off" depending on whether the element is checked or not.
     *
     * @return the element value, or "on/off" for checkbox/radio elements
     */
    public String getValue() {
        return getAttribute("value");
    }

    public WebObject and() {
        return this;
    }

    public WebObject then() {
        return this;
    }

    /**
     * The click function and wait for page to load
     */
    public void click() {
        getElement().click();
    }

    /**
     * This method will perform double click on the element using {@link Actions}
     */
    public void actionDoubleClick() {
        new Actions(driver).doubleClick(getElement()).build().perform();
    }

    /**
     * This method will perform single click on the element using {@link Actions}
     */
    public void actionClick() {
        new Actions(driver).click(getElement()).build().perform();
    }

    /**
     * This method will clear input field and send given text to it.
     */
    public void type(String value) {
        webElement = getElement();
        webElement.clear();
        webElement.sendKeys(value);
    }

    public void clearWithKeys() {
        webElement = getElement();
        webElement.sendKeys(Keys.CONTROL, "a");
        webElement.sendKeys(Keys.DELETE);
    }

    /**
     * This method will send given sequence of characters to the element.
     */
    public void sendKeys(CharSequence... keysToSend) {
        getElement().sendKeys(keysToSend);
    }

    /**
     * This method will clear input field.
     */
    public void clear() {
        getElement().clear();
    }

    //TODO
    private void throwErrorWithCauseIfPresent(Throwable timeout, String defaultMessage) {
        String timeoutMessage = timeout.getCause() != null ? timeout.getCause().getMessage() : timeout.getMessage();
        String finalMessage = StringUtils.isNotEmpty(timeoutMessage) ? timeoutMessage : defaultMessage;
        throw new ElementNotVisibleException(finalMessage, timeout);
    }

    /**
     * This method will wait until element will be clickable.
     */
    public void waitUntilClickable() {
        WebDriverWait wait = new WebDriverWait(driver, ConfigProvider.IMPLICIT_WAIT_SECONDS);
        wait.until(ExpectedConditions.elementToBeClickable(getElement()));
    }

    /**
     * This method will wait until element will be invisible.
     */
    public void waitUntilNotVisible() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, ConfigProvider.IMPLICIT_WAIT_SECONDS);
            String locatorType = selectorPair.getKey().getValue();
            String locatorValue = selectorPair.getValue();
            if (locatorType.equals("css")) {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(locatorValue)));
            } else if (locatorType.equals("xpath")) {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(locatorValue)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Move mouse to the element using {@link Actions}
     */
    public void mouseHover() {
        new Actions(driver).moveToElement(getElement()).build().perform();
    }

    /**
     * This method will execute click using javascript on the element.
     */
    public void clickWithJS() {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        String locatorType = selectorPair.getKey().getValue();
        String locatorValue = selectorPair.getValue();
        if (locatorType.equals("xpath")) {
            jse.executeScript("arguments[0].click();", getElement());
        } else {
            jse.executeScript("document.querySelector(\"" + locatorValue + "\").click();");
        }
    }

    /**
     * This method will scroll page to the element.
     */
    public void scrollToElement() {
        int elementY = getElement().getLocation().getY();
        int currentLocation = Integer.parseInt(WebDriverUtils.executeJS("return document.documentElement.scrollTop;"));
        int visibleY = (Settings.isDesktop()) ? 800 : 500;
        Logger.debug("elementY: " + elementY);
        Logger.debug("currentLocation: " + currentLocation);
        if (elementY > visibleY || currentLocation > 10) {
            WebDriverUtils.scrollPage(elementY - 150);
        }
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        return null;
    }

    /**
     * This method will move slider element to given offset using {@link Action}
     */
    public void moveSlider(int xOffset, int yOffset) {
        new Actions(driver).dragAndDropBy(getElement(), xOffset, yOffset).build().perform();
    }
}
