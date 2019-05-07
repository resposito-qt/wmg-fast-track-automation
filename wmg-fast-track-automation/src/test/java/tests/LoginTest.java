package tests;

import framework.platform.SiteNavigatorBase;
import framework.zapi.Zephyr;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;


/**
 * LoginTest
 */
public class LoginTest {

    @AfterMethod(alwaysRun = true) // always run 'true' to run after each test
    public void afterMethod() {
        System.out.println("afterMethod");
    }

    @Test(groups = "integration") // use custom groups to run the single test locally
    @Zephyr(key = "AS-3459") // jira issue number for Zephyr reporter
    public void loginPage_loginWithValidCreds_loginSuccessful() {
        SiteNavigatorBase.goToLoginPage().login();
    }
}
