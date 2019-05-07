package framework.adapters;

import framework.Logger;
import framework.platform.ConfigProvider;
import framework.zapi.ZephyrReporter;
import org.testng.ISuite;
import org.testng.ITestResult;


/**
 * One of the framework core classes.
 * <p/>
 * Custom listener which is used in test execution analysis and generation of custom reports.
 */
public class ServiceTestListener extends BaseAutomationTestListener {

    /**
     * Overload of testng method which gathers information for each failed test.
     */
    @Override
    public void onTestFailure(ITestResult result) {
        Logger.err("TEST " + result.getName() + " FAILED!");
        Logger.err("REASON: " + result.getThrowable().getLocalizedMessage());
        StringBuilder error = new StringBuilder();
        for (StackTraceElement element : result.getThrowable().getStackTrace()) {
            error.append(element.toString()).append("\n");
        }
        Logger.err("STACK TRACE:\n" + error);
    }

    @Override
    public void onStart(ISuite iSuite) {
        Logger.info("Test suite has been started");
    }

    @Override
    public void onFinish(ISuite iSuite) {
        if (!ConfigProvider.isLocalRun) ZephyrReporter.publishResultsToZephyr(testResults);
    }
}
