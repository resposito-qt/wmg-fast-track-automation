package framework.adapters;

import framework.Logger;
import framework.Settings;
import framework.platform.ConfigProvider;
import framework.platform.DatePatterns;
import framework.platform.Device;
import framework.zapi.TestResult;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.joda.time.DateTime;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.*;
import java.util.*;

import static org.testng.Assert.assertEquals;


/**
 * One of the framework core classes.
 * <p>
 * Custom listener which is used in test execution analysis and generation of custom reports.
 */
public abstract class BaseAutomationTestListener extends TestListenerAdapter implements IReporter, ISuiteListener {

    private static final String HTML_CAPTURE_PATH = "target/surefire-reports/failed/html";
    protected String screenFilePath;
    private String htmlFilePath;
    DateTime dateTime = new DateTime();
    private static String buildUrl = getFixedBuildUrl();

    public static List<TestResult> testResults = new ArrayList<>();

    private static String getFixedBuildUrl() {
        String url = System.getenv("BUILD_URL");
        if (url != null) {
            url = url.replace("http:", "");
        }
        return url;
    }

    /**
     * Overload of the testng method which provides generation of report about current test run.
     *
     * @param xmlSuites
     * @param suites
     * @param outputDirectory
     */
    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        assertEquals(suites.size(), 1, "Multiple test suites are not supported");
        assertEquals(suites.get(0).getResults().size(), 1, "Multiple test results per suite are not supported");
        ITestContext testContext = suites.get(0).getResults().values().iterator().next().getTestContext();

        initVelocity();
        VelocityContext velocityContext = createVelocityContext(testContext);
        saveReport(velocityContext, new File(new File(outputDirectory), "custom-report.html"));
    }

    /**
     * Initialization of template engine that will be used in report generation.
     * <p>
     * For additional information please visit http://velocity.apache.org/
     */
    private void initVelocity() {
        Velocity.setProperty("resource.loader", "classpath");
        Velocity.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        Velocity.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
        try {
			Velocity.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Method which saves report on local machine.
     */
    private void saveReport(VelocityContext context, File outputFile) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            Velocity.mergeTemplate("report.html", "UTF-8", context, writer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Saves information about all passed tests into list of {@link TestInfo}.
     */
    private Collection<TestInfosList> createTestInfos(Set<ITestResult> tests) {
        Map<String, TestInfosList> testInfosMap = new HashMap<String, TestInfosList>();

        for (ITestResult test : tests) {
            TestInfo testInfo = new TestInfo();
            testInfo.setName(getMethodName(test));
            testInfo.setDuration(String.format("%.3f", (test.getEndMillis() - test.getStartMillis()) / 1000.0));
            try {
                testInfo.parseDescription(test.getMethod().getMethodName() + " " + "C ");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if (test.getThrowable() != null) {
                StringWriter writer = new StringWriter();
                test.getThrowable().printStackTrace(new PrintWriter(writer));
                testInfo.setStackTrace(writer.toString().replace("<", "[").replace(">", "]"));
                if (ConfigProvider.browser != null) {
                    if (buildUrl == null) {
                        testInfo.setScreenshotUrl("file:///" + screenFilePath + getScreenshotFilename(test));
                        testInfo.setHTMLUrl("file:///" + htmlFilePath + getMethodName(test) + ".html");
                    } else {
                        testInfo.setScreenshotUrl(buildUrl
                                + "../ws/test/integration/target/surefire-reports/failed/"
                                + getScreenshotFilename(test));
                        testInfo.setHTMLUrl(buildUrl
                                + "../ws/test/integration/target/surefire-reports/failed/html/"
                                + getMethodName(test)
                                + ".html");
                    }
                }
            }
            if (testInfosMap.containsKey(test.getTestClass().getName())) {
                TestInfosList temp = testInfosMap.get(test.getTestClass().getName());
                temp.getTestInfos().add(testInfo);
            } else {
                TestInfosList temp = new TestInfosList(test.getTestClass().getName());
                temp.getTestInfos().add(testInfo);
                testInfosMap.put(temp.getClassName(), temp);
            }
            testInfo.setLog(Logger.getTestInfoLogsList(test));
        }
        return testInfosMap.values();
    }

    /**
     * Return test method name.
     */
    private static String getMethodName(ITestResult result) {
        String className = result.getTestClass().getName();
        return className.substring(className.lastIndexOf('.') + 1) + "." + result.getName();
    }

    /**
     * Sets variables that will be used in /target/report.html
     */
    private VelocityContext createVelocityContext(ITestContext testContext) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("timeStart", dateTime.toString(DatePatterns.MM_dd_yyyy_HH_mm.getPattern(), Locale.US));
        templateParams.put("testGroups", testContext.getIncludedGroups());
        templateParams.put("project", ConfigProvider.project);
        templateParams.put("environment", ConfigProvider.componentDomain);
        if (ConfigProvider.browser != null) {
            if (Settings.getPlatform().equals(Device.DESKTOP)) {
                templateParams.put("platformVersion", "");
            } else {
                templateParams.put("platformVersion", ConfigProvider.platformVersion);
            }
            templateParams.put("platform", Settings.getPlatform().toString());
            templateParams.put("browser", ConfigProvider.browser);
        }
        templateParams.put("numberOfFailed", testContext.getFailedTests());
        templateParams.put("numberOfSuccess", testContext.getPassedTests());
        templateParams.put("numberOfSkipped", testContext.getSkippedTests());
        templateParams.put("numberOfTests", testContext.getSkippedTests().size() + testContext.getFailedTests().size() + testContext.getPassedTests().size());
        templateParams.put("numberOfThreads", ConfigProvider.threadsCount);
        templateParams.put("duration", String.format("%.3f", (testContext.getEndDate().getTime() - testContext.getStartDate().getTime()) / 60000.0));
        templateParams.put("failedTests", createTestInfos(testContext.getFailedTests().getAllResults()));
        templateParams.put("failedConfigurations", createTestInfos(testContext.getFailedConfigurations().getAllResults()));
        templateParams.put("skippedTests", createTestInfos(testContext.getSkippedTests().getAllResults()));
        templateParams.put("passedTests", createTestInfos(testContext.getPassedTests().getAllResults()));
        if (buildUrl == null) {
            templateParams.put("cssLocation", "../test-classes/style.css");
        } else {
            templateParams.put("cssLocation", buildUrl + "../ws/test/integration/target/test-classes/style.css");
        }
        return new VelocityContext(templateParams);
    }

    /**
     * Overload of testng method which prints logs for each successful test.
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        Logger.info(String.format("%s - Test %s PASSED\n", getCurrentTime(), result.getName()));
    }

    /**
     * Sets filename of screenshot equals to test method name.
     */
    protected String getScreenshotFilename(ITestResult result) {
        return getMethodName(result) + ".png";
    }

    /**
     * Will save HTML of page with filename same as test method name.
     */
    protected void savePageHTML(ITestResult result) {
        htmlFilePath = savePageHtml(getMethodName(result));
    }

    /**
     * Will save HTML of page.
     */
    public static String savePageHtml(String filename) {
        String htmlFilePath = new File(HTML_CAPTURE_PATH).getAbsolutePath() + "/";
        File htmlDirectoryPath = new File(HTML_CAPTURE_PATH);
        if (!htmlDirectoryPath.exists()) {
            if (!htmlDirectoryPath.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + htmlDirectoryPath + ".");
            }
        }
        try (FileWriter innerHTMLWriter = new FileWriter(htmlFilePath + filename + ".html")) {
            String innerHTML = WebDriverManager.getDriver().getPageSource();
            innerHTMLWriter.write(innerHTML);
        } catch (Exception e) {
            Logger.info("Failed to get or save HTML: " + e + ". See screenshot.");
        }
        return htmlFilePath;
    }

    /**
     * Will capture and save screenshot.
     */
    protected void captureDefaultScreenShot(String screenShotPath) {
        try {
            File scrFile = WebDriverManager.getDriver().getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File(screenShotPath));
        } catch (Exception screenException) {
            Logger.info("ScreenShot can not be saved.");
            screenException.printStackTrace();
        }
    }

    /**
     * Overload of testng method which prints out time and name for each started test.
     */
    @Override
    public void onTestStart(ITestResult result) {
        Logger.info(String.format("\n%s - Test %s STARTED", getCurrentTime(), result.getName()));
    }

    /**
     * Will create screenshot and save it to "target/surefire-reports/screenshots".
     */
    public void createScreenshot(String screenName) {
        try {
            File scrFile = WebDriverManager.getDriver().getScreenshotAs(OutputType.FILE);
            String destDir = "target/surefire-reports/screenshots";
            boolean isFolderExists = (new File(destDir).exists());

            if (!isFolderExists) {
                boolean isFolder = (new File(destDir).mkdirs());
                Assert.assertTrue(isFolder, "Folder was not created");
            }
            String destFile = String.format("%s/%s.png", destDir, screenName);
            FileUtils.copyFile(scrFile, new File(destFile));
        } catch (WebDriverException | IOException e) {
            e.printStackTrace();
            Logger.info("Screenshot was not created. Due to an error: " + e.getMessage());
        }
    }

    /**
     * Will return formatted current time.
     */
    protected String getCurrentTime() {
        DateTime currentTime = new DateTime();
        return currentTime.toString("E MMM, d hh:mm:ss", Locale.US);
    }
}
