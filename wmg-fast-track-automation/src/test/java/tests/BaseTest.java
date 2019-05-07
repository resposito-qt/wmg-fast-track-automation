package tests;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import framework.platform.ConfigProvider;
import framework.platform.SiteNavigatorBase;
import framework.platform.utilities.RequestUtils;
import framework.platform.utilities.WebDriverUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import pages.HomePage;

public abstract class BaseTest {

    public static RequestUtils requestUtils = RequestUtils.getInstance();
    public static Gson gson = new Gson();
    public static JsonParser jsonParser = new JsonParser();
    public static String serviceUrl = ConfigProvider.serviceUrl + "/api/v1/";
    protected static HomePage homePage;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        homePage = loginToApp();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        if (!WebDriverUtils.getCurrentUrl().endsWith(".com/")) homePage = SiteNavigatorBase.goToHomePage();
    }

    protected HomePage loginToApp() {
        return SiteNavigatorBase.goToLoginPage().login();
    }
}
