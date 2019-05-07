package framework.adapters;

import framework.Logger;
import framework.Settings;
import framework.platform.ConfigProvider;
import framework.zapi.ZephyrReporter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 * One of the framework core classes.
 * <p/>
 * Custom listener which is used in test execution analysis and generation of custom reports.
 */
public class UIAutomationTestListener extends BaseAutomationTestListener {

    /**
     * Overload of testng method which gathers information for each failed test.
     */
    @Override
    public void onTestFailure(ITestResult result) {
        Logger.err("TEST " + result.getName() + " FAILED!");
        Logger.err("REASON: " + result.getThrowable().getLocalizedMessage());
        try {
            Logger.err("CURRENT URL: " + WebDriverManager.getDriver().getCurrentUrl());
        } catch (Exception e) {
            Logger.debug("URL cannot be taken");
        }
        StringBuilder error = new StringBuilder();
        for (StackTraceElement element : result.getThrowable().getStackTrace()) {
            error.append(element.toString()).append("\n");
        }
        Logger.err("STACK TRACE:\n" + error);

        screenFilePath = new File("target/surefire-reports/failed").getAbsolutePath() + "/";
        String screenshotPath = screenFilePath + getScreenshotFilename(result);
        captureDefaultScreenShot(screenshotPath);
        savePageHTML(result);
    }

    @Override
    public void onStart(ISuite iSuite) {
        Logger.info("Test suite has been started");
        RemoteWebDriver driver = Settings.createInstance();
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(ConfigProvider.IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS);
        if (Settings.isDesktop()) {
            driver.manage().window().maximize();
        }
        iSuite.setAttribute("driver", driver);
    }

    @Override
    public void onFinish(ISuite iSuite) {
        Object driver = iSuite.getAttribute("driver");
        if (driver == null) {
            return;
        }
        if (!(driver instanceof RemoteWebDriver)) {
            throw new IllegalStateException("Corrupted WebDriver.");
        }
        iSuite.setAttribute("driver", null);
        try {
            ((RemoteWebDriver) driver).quit();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.info("Can not quit driver");
        } finally {
            if (!ConfigProvider.isLocalRun) ZephyrReporter.publishResultsToZephyr(testResults);
        }
    }

    public static RemoteWebDriver getDriver() {
        ITestResult result = Reporter.getCurrentTestResult();
        Object driver = result.getTestContext().getSuite().getAttribute("driver");
        if (driver == null) {
            throw new IllegalStateException("Unable to find a valid webdriver instance");
        }
        if (!(driver instanceof RemoteWebDriver)) {
            throw new IllegalStateException("Corrupted WebDriver.");
        }
        return (RemoteWebDriver) driver;
    }
}
