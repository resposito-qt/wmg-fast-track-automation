package framework.zapi;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import framework.Logger;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Zapi {

    private static String VERSION = "-1";
    private static String cycleId;
    private static JsonParser jsonParser = new JsonParser();
    private static Request request = Request.getInstance();
    private static String build_url = System.getenv("BUILD_URL");
    private static String build_number = System.getenv("BUILD_NUMBER");
    private static String projectKey = System.getProperty("project.key");
    private static String projectId;

    public static void createNewTestCycle(String testCycleName) {
        Logger.info(String.format("Getting project id for projectKey: %s", projectKey));
        String jiraProjectEndpoint = "/rest/api/2/project/" + projectKey.toUpperCase();
        String response = request.performHttpRequest("GET", jiraProjectEndpoint).getBody();
        projectId = jsonParser.parse(response).getAsJsonObject().get("id").getAsString();

        Logger.info(String.format("Creating new Test Cycle with name: %s, projectId: %s", testCycleName, projectId));
        JsonObject requestBody = new JsonObject();

        requestBody.addProperty("clonedCycleId", "");
        requestBody.addProperty("name", testCycleName + " " + build_number);
        requestBody.addProperty("build", "");
        requestBody.addProperty("environment", "");
        requestBody.addProperty("description", build_url);
        requestBody.addProperty("projectId", projectId);
        requestBody.addProperty("versionId", VERSION); // unversioned
        requestBody.addProperty("startDate", "");
        requestBody.addProperty("endDate", "");

        response = request.performHttpRequest("POST", "/rest/zapi/latest/cycle", requestBody.toString()).getBody();
        cycleId = jsonParser.parse(response).getAsJsonObject().get("id").getAsString();
    }

    public static void addTestsToCycle(List<String> issues) {
        Logger.info(String.format("Adding test(s) to cycle for project - %s: %s", projectKey, issues.toString()));
        String addTestToCycleEndPoint = "/rest/zapi/latest/execution/addTestsToCycle";
        JsonObject requestBody = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        for (String issue : issues) {
            jsonArray.add(new JsonPrimitive(issue));
        }

        requestBody.add("issues", jsonArray);
        requestBody.addProperty("versionId", VERSION);
        requestBody.addProperty("cycleId", cycleId);
        requestBody.addProperty("projectId", projectId);
        requestBody.addProperty("method", "1");

        request.performHttpRequest("POST", addTestToCycleEndPoint, requestBody.toString());
    }

    private static String getIssueId(String issueKey) {
        Logger.info("Getting id for issue: " + issueKey);
        String issueIdEndPoint = "/rest/api/2/issue/" + issueKey;
        String issueId = null;
        try {
            String response = request.performHttpRequest("GET", issueIdEndPoint).getBody();
            issueId = jsonParser.parse(response).getAsJsonObject().getAsJsonPrimitive("id").getAsString();
        } catch (Exception e) {
            Logger.err("Can't find an issue with key: " + issueKey);
        }
        return issueId;
    }

    public static void executeTests(List<TestResult> results) {
        String response;
        String executionId;
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        for (TestResult result : results) {
            int testResult = result.getResultStatus();
            String issueNumber = result.getIssueNumber();
            String createExecutionEndPoint = "/rest/zapi/latest/execution/";
            JsonObject requestBody = new JsonObject();

            String issueId = getIssueId(issueNumber);
            if (issueId == null) {
                continue;
            }
            requestBody.addProperty("issueId", issueId);
            requestBody.addProperty("versionId", VERSION);
            requestBody.addProperty("cycleId", cycleId);
            requestBody.addProperty("projectId", projectId);

            response = request.performHttpRequest("POST", createExecutionEndPoint, requestBody.toString()).getBody();
            Map<String, Object> data = new Gson().fromJson(response, type);
            executionId = data.entrySet().iterator().next().getKey();

            String quickExecuteTestEndPoint = "/rest/zapi/latest/execution/" + executionId + "/execute";

            requestBody = new JsonObject();

            requestBody.addProperty("status", testResult);
            request.performHttpRequest("PUT", quickExecuteTestEndPoint, requestBody.toString());
        }
    }
}
