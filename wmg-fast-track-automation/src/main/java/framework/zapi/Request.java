package framework.zapi;

import framework.Logger;
import framework.platform.ConfigProvider;
import framework.platform.utilities.CustomResponseHandler;
import framework.platform.utilities.Response;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class Request {

    private static Request request = new Request();

    public static String jiraUrl;
    public static final String JIRA_USER = "bts.dashboard.folio";
    public static final String JIRA_PASS = "dsp@4099";

    protected CloseableHttpClient httpClient;
    private CustomResponseHandler handler = new CustomResponseHandler();

    public static Request getInstance() {
        return request;
    }

    private Request() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(30000).setConnectTimeout(30000).setSocketTimeout(30000).build();
        CookieStore cookieStore = new BasicCookieStore();

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, (arg0, arg1) -> true).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }

        httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(requestConfig)
                .setSslcontext(sslContext)
                .build();
        getAccess();
    }

    private void getAccess() {
        new ConfigProvider();
        jiraUrl = ConfigProvider.jiraUrl;
        performHttpRequest("POST", "?os_username=" + JIRA_USER + "&os_password=" + JIRA_PASS, null);
    }

    protected Response performHttpRequest(String type, String url) {
        return performHttpRequest(type, url, null);
    }

    protected Response performHttpRequest(String type, String url, String body) {
        String uri = jiraUrl + url;
        Logger.info(">>>>> Performing HTTP " + type + ": " + uri + " with body: " + body);
        HttpEntityEnclosingRequestBase requestBase = null;
        try {
            if (type.equals("GET")) {
                HttpGet httpGet = new HttpGet(uri);
                return handler.handleResponse(httpClient.execute(httpGet));
            } else if (type.equals("POST")) {
                requestBase = new HttpPost(uri);
            } else if (type.equals("PUT")) {
                requestBase = new HttpPut(uri);
            }
            if (requestBase != null && body != null) {
                StringEntity jsonEntity = new StringEntity(body);
                requestBase.setHeader("Content-type", "application/json");
                requestBase.setEntity(jsonEntity);
            }
            return handler.handleResponse(httpClient.execute(requestBase));
        } catch (Exception ex) {
            Logger.err("Exception while performing HTTP " + type + ": " + ex.getMessage());
            throw new RuntimeException(ex);
        } finally {
            if (requestBase != null) {
                requestBase.releaseConnection();
            }
        }
    }
}
