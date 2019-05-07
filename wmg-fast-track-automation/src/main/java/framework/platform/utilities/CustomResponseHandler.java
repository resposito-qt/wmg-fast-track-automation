package framework.platform.utilities;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CustomResponseHandler {

    protected static final Logger logger = LoggerFactory.getLogger(CustomResponseHandler.class);

    public Response handleResponse(HttpResponse httpResponse) throws IOException {
        Response response = new Response();
        response.setBody(EntityUtils.toString(httpResponse.getEntity()));
        response.setCode(httpResponse.getStatusLine().getStatusCode());
        return response;
    }
}