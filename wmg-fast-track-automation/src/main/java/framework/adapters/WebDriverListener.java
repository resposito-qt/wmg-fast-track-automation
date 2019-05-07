package framework.adapters;

import framework.platform.ConfigProvider;
import framework.zapi.TestResult;
import framework.zapi.Zephyr;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

/**
 * One of the framework core classes.
 * <p>
 * Custom listener which is used for instantiation of new WebDriver for each test method.
 */
public class WebDriverListener implements IInvokedMethodListener {

    /**
     * Overload of the testng listener that will start new instance of webdriver
     */
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!ConfigProvider.isLocalRun) {
            Method m = testResult.getMethod().getConstructorOrMethod().getMethod();
            Zephyr zephyr = m.getAnnotation(Zephyr.class);
            if (zephyr != null) {
                String key = zephyr.key();
                int status = testResult.getStatus();
                ServiceTestListener.testResults.add(new TestResult(key, status));
            }
        }
    }
}
