package framework.components;

import framework.Logger;
import framework.Settings;
import framework.adapters.WebDriverManager;
import framework.platform.ConfigProvider;
import framework.platform.html.SelectorType;
import framework.platform.html.WebObject;
import framework.platform.html.support.HtmlElementUtils;
import framework.platform.web.Locator;
import javafx.util.Pair;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Base class from which all page classes should be derived.
 * <p>
 * It contains the code to initialize {$project}.pages, initialize WebObject's, and interact in various ways with the
 * page(s).
 */
public abstract class PageObject extends AbstractPage {
    public static RemoteWebDriver driver;

    /**
     * Constructor.
     */
    public PageObject() {
        super();
        driver = WebDriverManager.getDriver();
        initializeWebObjects(this);
        waitForPageToLoad();
    }

    @Override
    public void waitForPageToLoad() {
        waitForAngularRequestsToFinish();
    }

    public PageObject scrollDownThePage() {
        scrollPage(1000);
        waitForAjaxRequestToBeFinished();
        return this;
    }

    /**
     * Will return HTML of the page.
     */
    public String getPageSource() {
        return driver.getPageSource();
    }

    public static void waitForAjaxRequestToBeFinished() {
        new WebDriverWait(driver, ConfigProvider.IMPLICIT_WAIT_SECONDS).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                return (Boolean) js.executeScript("return jQuery.active == 0");
            }
        });
    }

    public void waitForAngularRequestsToFinish() {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        if ((Boolean) jse.executeScript("return (typeof angular !== \'undefined\')? true : false;")) {
            jse.executeAsyncScript("var callback = arguments[arguments.length - 1]" +
                    ";angular.element(document.body).injector().get(\'$browser\').notifyWhenNoOutstandingRequests(callback);");
        }

    }

    /**
     * Stops program execution for specified amount of time.
     *
     * @param milliseconds Amount of time to wait in milliseconds (1000 = 1 second)
     */
    public static void waitABit(int milliseconds) {
        Logger.info("Waiting for " + milliseconds + " milliseconds");
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialization of all fields of type {@link framework.platform.html.WebObject} which are complemented with the annotation {@link framework.platform.web.Locator}.
     *
     * @param whichClass Page class to parse.
     */
    public void initializeWebObjects(Object whichClass) {
        Class<?> incomingClass = whichClass.getClass();
        ArrayList<Field> fields = new ArrayList<>();

        Class<?> tempIncomingClass = incomingClass;
        do {
            fields.addAll(Arrays.asList(tempIncomingClass.getDeclaredFields()));
            tempIncomingClass = tempIncomingClass.getSuperclass();
        } while (tempIncomingClass != null);

        String errorDesc = " while initializing locators for WebObjects. Root cause:";
        try {
            for (Field field : fields)
                if (field.isAnnotationPresent(Locator.class)) {
                    Annotation annotation = field.getAnnotation(Locator.class);
                    Locator locatorAnnotation = (Locator) annotation;

                    field.setAccessible(true);

                    String locator = null;
                    switch (Settings.getPlatform()) {
                        case DESKTOP:
                            locator = locatorAnnotation.main();
                            break;
                        case MOBILE:
                            if (locatorAnnotation.mobile().isEmpty()) {
                                locator = locatorAnnotation.main();
                            } else {
                                locator = locatorAnnotation.mobile();
                            }
                            break;
                        case TABLET:
                            if (locatorAnnotation.mobile().isEmpty()) {
                                locator = locatorAnnotation.main();
                            } else {
                                locator = locatorAnnotation.tablet();
                            }
                            break;
                        default:
                            break;
                    }

                    Class<?> dataMemberClass = Class.forName(field.getType().getName());
                    Class<?> parameterTypes[] = new Class[2];
                    parameterTypes[0] = String.class;
                    parameterTypes[1] = String.class;
                    Constructor<?> constructor = dataMemberClass.getDeclaredConstructor(parameterTypes);

                    Object[] constructorArgList = new Object[2];

                    constructorArgList[0] = locator;
                    constructorArgList[1] = field.getName();
                    Object retobj = constructor.newInstance(constructorArgList);
                    field.set(whichClass, retobj);
                }
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException("Class not found" + errorDesc + exception, exception);
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException("An illegal argument was encountered" + errorDesc + exception, exception);
        } catch (InstantiationException exception) {
            throw new RuntimeException("Could not instantantiate object" + errorDesc + exception, exception);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Could not access data member" + errorDesc + exception, exception);
        } catch (InvocationTargetException exception) {
            throw new RuntimeException("Invocation error occured" + errorDesc + exception, exception);
        } catch (SecurityException exception) {
            throw new RuntimeException("Security error occured" + errorDesc + exception, exception);
        } catch (NoSuchMethodException exception) {
            throw new RuntimeException("Method specified not found" + errorDesc + exception, exception);
        }
    }

    /**
     * Will scroll the page to specified Y coordinate point.
     */
    public void scrollPage(Integer y) {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("scroll(0, " + y.toString() + ");");
    }

    /**
     * Will switch focus to default contents of the page.
     */
    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    /**
     * Will navigate back to previous visited web page.
     *
     * @param expectedPage Page class that stands for previous page
     * @return Initialized page object.
     */
    public <T> T clickBackBrowserButton(Class<T> expectedPage) {
        Logger.info("Click 'Back' browser button");
        WebDriverManager.getDriver().navigate().back();
        waitForAjaxRequestToBeFinished();
        return PageFactory.initElements(driver, expectedPage);
    }

    /**
     * Will refresh current web page.
     *
     * @return Instance of current page.
     */
    public PageObject refresh() {
        Logger.info("Refresh page");
        driver.navigate().refresh();
        this.waitForPageToLoad();
        return this;
    }

    /**
     * Checks for presence of alert.
     */
    protected boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
        } catch (NoAlertPresentException exception) {
            return false;
        }
        return true;
    }

    /**
     * This method will return number of elements on page.
     */
    public int getElementsCount(String selectorString) {
        try {
            return findElements(selectorString).size();
        } catch (NoSuchElementException ignored) {
            return 0;
        }
    }

    /**
     * Instance method used to call static class method locateElement.
     *
     * @return the web element found by selector
     */
    public WebObject findElement(String selectorString) {
        return findElement(selectorString, ConfigProvider.IMPLICIT_WAIT_SECONDS);
    }

    public WebObject findElement(String selectorString, int implicitTimeoutSeconds) {
        Pair<SelectorType, String> selectorTypeStringPair = resolveSelector(selectorString);
        WebElement foundElement = HtmlElementUtils.locateElement(selectorTypeStringPair, implicitTimeoutSeconds);
        return new WebObject(selectorTypeStringPair, foundElement);
    }

    /**
     * Instance method used to call static class method locateElements.
     *
     * @return the list of web elements found by selector
     */
    public List<WebObject> findElements(String selectorString) {
        Pair<SelectorType, String> selectorTypeStringPair = resolveSelector(selectorString);
        List<WebElement> foundElements = HtmlElementUtils.locateElements(selectorTypeStringPair);
        List<WebObject> webObjects = new ArrayList<>();
        if (foundElements != null) {
            foundElements.forEach(e -> webObjects.add(new WebObject(selectorTypeStringPair, e)));
        }
        return webObjects;
    }

    public static Pair<SelectorType, String> resolveSelector(String selectorString) {
        Pair<SelectorType, String> selector;
        if (selectorString.startsWith("./") || selectorString.startsWith("//")) {
            selector = new Pair<>(SelectorType.XPATH, selectorString);
        } else {
            selector = new Pair<>(SelectorType.CSS, selectorString);
        }
        return selector;
    }
}
