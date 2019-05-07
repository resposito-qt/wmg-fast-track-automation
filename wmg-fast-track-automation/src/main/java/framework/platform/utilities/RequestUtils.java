package framework.platform.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import framework.Logger;
import framework.platform.ConfigProvider;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestUtils {
    protected CloseableHttpClient httpClient;
    private CustomResponseHandler handler = new CustomResponseHandler();
    private String token;

    private static RequestUtils requestUtils = new RequestUtils();

    public static RequestUtils getInstance() {
        return requestUtils;
    }

    private RequestUtils() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(30000).setConnectTimeout(30000).setSocketTimeout(30000).build();

        SSLConnectionSocketFactory sslsf = null;
        SSLContext sslContext = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            sslsf = new SSLConnectionSocketFactory(
                    builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            sslContext = builder.loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }

        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setSSLSocketFactory(sslsf)
                .setSslcontext(sslContext)
                .build();
        try {
            token = getToken();
        } catch (IOException e) {
            throw new RuntimeException("Unable to get token to access the service");
        }
    }

    public Response performHttpRequest(Enum<Request> type, String url) {
        return performHttpRequest(type, url, null);
    }

    public Response performHttpRequest(Enum<Request> type, String url, String body) {
        Logger.info("Performing HTTP " + type + ": " + url + " with body: " + body);

        Header[] headers = {
                new BasicHeader("Authorization", "Bearer " + token)
                , new BasicHeader("Accept", "application/json")
                , new BasicHeader("User-Agent", "Apache-HttpClient/4.1.1 (java 1.5)")
                , new BasicHeader("Content-Type", "application/json")
        };

        HttpEntityEnclosingRequestBase requestBase = null;
        HttpRequestBase httpRequestBase = null;
        try {
            if (type.equals(Request.GET) || type.equals(Request.DELETE)) {
                if (type.equals(Request.GET)) {
                    httpRequestBase = new HttpGet(url);
                } else {
                    httpRequestBase = new HttpDelete(url);
                }
                httpRequestBase.setHeaders(headers);
                return handler.handleResponse(httpClient.execute(httpRequestBase));
            } else if (type.equals(Request.POST)) {
                requestBase = new HttpPost(url);

            } else if (type.equals(Request.PUT)) {
                requestBase = new HttpPut(url);
            }
            if (requestBase != null && body != null) {
                StringEntity jsonEntity = new StringEntity(body);
                requestBase.setEntity(jsonEntity);
            }
            assert requestBase != null;
            requestBase.setHeaders(headers);
            return handler.handleResponse(httpClient.execute(requestBase));
        } catch (Exception ex) {
            Logger.err("Exception while performing HTTP " + type + ": " + ex.getMessage());
            throw new RuntimeException(ex);
        } finally {
            if (requestBase != null) {
                requestBase.releaseConnection();
            } else if (httpRequestBase != null) {
                httpRequestBase.releaseConnection();
            }
        }
    }

    /**
     * This functions gets token for test user
     *
     * @return
     * @throws Exception
     */
    private String getToken() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        UrlEncodedFormEntity entity;
        HttpPost post;
        HttpResponse response;
        Header[] responseCookies;
        String code;

        /**
         * login.do
         */
        post = new HttpPost(ConfigProvider.identityUrl + "/login.do");
        params.add(new BasicNameValuePair("username", ConfigProvider.testUser.getUsername()));
        params.add(new BasicNameValuePair("password", ConfigProvider.testUser.getPassword()));

        entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
        post.setEntity(entity);
        post.setHeader("Accept", "text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8");

        try {
            response = httpClient.execute(post);
            responseCookies = response.getHeaders("Set-Cookie");
        } finally {
            post.releaseConnection();
        }

        /**
         * oauth/
         */

        post = new HttpPost(ConfigProvider.identityUrl + "/oauth/authorize");
        post.setHeaders(responseCookies);

        params.add(new BasicNameValuePair("client_id", ConfigProvider.clientId));

        params.add(new BasicNameValuePair("redirect_uri", ConfigProvider.identityUrl));

        params.add(new BasicNameValuePair("response_type", "code"));
        params.add(new BasicNameValuePair("state", ConfigProvider.state));

        entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
        post.setEntity(entity);

        try {
            response = httpClient.execute(post);
            responseCookies = response.getHeaders("location");
            int start = responseCookies[0].getValue().indexOf("code");
            int end = responseCookies[0].getValue().indexOf("state");

            code = responseCookies[0].getValue().substring(start + 5, end - 1);

        } finally {
            post.releaseConnection();
        }

        /**
         * oauth/token
         */
        post = new HttpPost(ConfigProvider.identityUrl + "/oauth/token");
        post.setHeader("Accept", "application/json");
        post.setHeader("Authorization", "Basic " + Base64.encodeBase64String(
                (ConfigProvider.clientId + ":" + ConfigProvider.clientSecret).getBytes()));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("code", code));
        entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
        post.setEntity(entity);
        String token;
        try {
            response = httpClient.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());
            token = (String) mapper.readValue(responseBody, Map.class).get("access_token");
        } finally {
            post.releaseConnection();
        }
        return token;
    }
}

