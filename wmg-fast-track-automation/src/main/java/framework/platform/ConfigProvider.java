package framework.platform;

import com.google.gson.JsonParser;
import framework.Logger;
import framework.platform.utilities.CustomResponseHandler;
import models.User;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utilitarian class which provides initialization of all project properties as well access to them.
 */
public class ConfigProvider {

    private static Properties properties = readProperties();
    public static final int IMPLICIT_WAIT_SECONDS = 10;

    public static String grid = getConfigParameter("selenium.grid", "localhost");
    public static String port = getConfigParameter("selenium.grid.port", "4444");
    public static String platformVersion = getConfigParameter("platform.version", "null");
    public static String device = getConfigParameter("device", "iPhone 6");
    public static String browser = getConfigParameter("browser", "chrome");
    public static String project = getConfigParameter("project.name", "Project");
    public static String appiumUrl = getConfigParameter("appium.url", "http://localhost:4723/wd/hub");
    public static String threadsCount = getConfigParameter("thread.count", "1");
    public static boolean isLocalRun = true;
    //public static String componentDomain = getComponentDomain();
    	public static String componentDomain = "qa.wmg.com";
    public static String identityUrl = String.format(getConfigParameter("identity.url"), componentDomain);
    public static String baseUrl = String.format(getConfigParameter("base.url", "BaseUrlNotDefined"), componentDomain);
    public static String serviceUrl = String.format(getConfigParameter("service.url"), componentDomain); // Service url;
    public static User testUser = new User(getConfigParameter("test.user.name"), getConfigParameter("test.user.pass"));
    public static String clientId = getConfigParameter("client_id");
    public static String clientSecret = getConfigParameter("clientsecret");
    public static String state = getConfigParameter("state");
    public static String jiraUrl = getConfigParameter("jira.url");
    public static String dbUser = getConfigParameter("db.user", "");
    public static String dbPass = getConfigParameter("db.pass", "");
    public static String dbUrl = getConfigParameter("db.url", "");

    private static String getComponentDomain() {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CustomResponseHandler handler = new CustomResponseHandler();
        String url = System.getProperty("admin.console.url") + "/" + System.getProperty("environment.jenkins") + "/components/youtube?expand=spaces%2Cshared')";
        Logger.info("URL for component domain - " + url);
        HttpGet get = new HttpGet(url);
        String domain;
        try {
            domain = new JsonParser().parse(handler.handleResponse(httpClient.execute(get)).getBody())
                    .getAsJsonObject().getAsJsonPrimitive("domain").getAsString();
        } catch (IOException e) {
            throw new RuntimeException("Unable to get component domain!");
        }
        isLocalRun = false;
        Logger.info("Component domain - " + domain);
        return domain;
    }

    private static String getConfigParameter(String key) {
        return getConfigParameter(key, null);
    }

    private static String getConfigParameter(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            if (properties.getProperty(key) != null) {
                return properties.getProperty(key);
            } else if (defaultValue != null) {
                return defaultValue;
            }
            throw new RuntimeException("Configuration value not found for key '" + key + "'");
        }
        return value;
    }

    public static Properties readProperties() {
        try (InputStream propertyStream = ConfigProvider.class.getResourceAsStream("/project.properties")) {
            properties = new Properties();
            properties.load(propertyStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
