package framework;

import framework.platform.BrowserType;
import framework.platform.ConfigProvider;
import framework.platform.Device;
import framework.platform.UnknownBrowserException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import static org.testng.Assert.fail;

/**
 * Utilitarian class that provides initialization of browser for specified webdriver and
 * information about current environment.
 */
public class Settings {
    public static final BrowserType browser = BrowserType.Browser(ConfigProvider.browser);

    /**
     * Provides information about current platform.
     * @return
     *           Type of the current platform.
     */
    public static Device getPlatform() {
        if (isMobile()) {
            return Device.MOBILE;
        }
        if (isDesktop()) {
            return Device.DESKTOP;
        } else {
            return Device.TABLET;
        }
    }

    /** Checks whether is current platform Mobile or not. */
    public static boolean isMobile() {
        return (browser.equals(BrowserType.MOBILE_CHROME) || browser.equals(BrowserType.MOBILE_SAFARI)) && !isTablet();
    }

    /** Checks whether is current platform Desktop or not. */
    public static boolean isDesktop() {
        return !isMobile() && !isTablet();
    }

    /** Checks whether is current platform Tablet or not. */
    public static boolean isTablet() {
        return ConfigProvider.device.toLowerCase().contains("ipad")
                || ConfigProvider.device.equalsIgnoreCase("Nexus 7")
                || ConfigProvider.device.equalsIgnoreCase("Nexus 9");
    }

    /** Creates new instance of webdriver. */
    public static RemoteWebDriver createInstance() {
        return getDriver(browser);
    }

    /**
     * Provides information about current selenium grid hub URL.
     *
     * @return
     *          URL which is specific for current selenium grid hub.
     */
    private static URL getRemoteURL() {
        try {
            if (isMobile() || isTablet()) {
                return new URL(ConfigProvider.appiumUrl);
            } else {
                return new URL(String.format("http://%s:%s/wd/hub", ConfigProvider.grid, ConfigProvider.port));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail("There was an error. Please see log");
            return null;
        }
    }

    /**
     * Creates instance of webdriver for specific {@link BrowserType}
     *
     * @param browserType
     *          Type of browser which will be user for webdriver initialization.
     * @return
     *          New instance of webdriver.
     */
    private static RemoteWebDriver getDriver(BrowserType browserType) {
        DesiredCapabilities capabilities;
        String downloadFilepath = System.getProperty("user.dir") + File.separator + "target";

        Logger.info("Hub URL: " + getRemoteURL());
        switch (browserType) {
            case FIREFOX:
                capabilities = DesiredCapabilities.firefox();
                return new RemoteWebDriver(getRemoteURL(), capabilities);
            case FIREFOX_NO_GRID:
                return new FirefoxDriver();
            case CHROME:
                HashMap<String, Object> chromePrefs = new HashMap<>();
                chromePrefs.put("profile.default_content_settings.popups", 0);
                chromePrefs.put("download.default_directory", downloadFilepath);
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setExperimentalOption("prefs", chromePrefs);
                chromeOptions.addArguments("ignore-certificate-errors");
                return new ChromeDriver(chromeOptions);
            case SAFARI:
                capabilities = DesiredCapabilities.safari();
                return new RemoteWebDriver(getRemoteURL(), capabilities);
            case IE:
                capabilities = DesiredCapabilities.internetExplorer();
                capabilities.setCapability("nativeEvents", false);
                capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
                return new RemoteWebDriver(getRemoteURL(), capabilities);
            case MOBILE_CHROME:
                capabilities = DesiredCapabilities.android();
                capabilities.setCapability("browserName", "chrome");
                capabilities.setCapability("device", "android");
                capabilities.setCapability("newCommandTimeout", "180");
                capabilities.setCapability("platformVersion", "5.1");
                capabilities.setCapability("platformName", org.openqa.selenium.Platform.ANDROID);
                capabilities.setCapability("deviceName", ConfigProvider.device);
                return new AndroidDriver(getRemoteURL(), capabilities);
            case MOBILE_SAFARI:
                capabilities = DesiredCapabilities.iphone();
                capabilities.setCapability("browserName", "safari");
                capabilities.setCapability("device", "iphone");
                capabilities.setCapability("platformName", "ios");
                capabilities.setCapability("platformVersion", ConfigProvider.platformVersion);
                capabilities.setCapability("deviceName", ConfigProvider.device);
                capabilities.setCapability("newCommandTimeout", "180");
                capabilities.setCapability("autoAcceptAlerts", "true");
                return new IOSDriver(getRemoteURL(), capabilities);
            default:
                throw new UnknownBrowserException("Cannot create driver for unknown browser type");
        }
    }

    /**
     * Provides information about default environment URL.
     * @return
     *          URL which is default for specified environment.
     */
    public static String getDefaultUrl() {
        return ConfigProvider.baseUrl;
    }
}
