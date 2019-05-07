package framework.zapi;

import framework.Logger;

import java.lang.Exception;
import java.util.List;
import java.util.stream.Collectors;

public class ZephyrReporter {

    private static final String TEST_PLAN_NAME = System.getProperty("test.plan.name");

    public static void publishResultsToZephyr(List<TestResult> results) {
        if (results.isEmpty()) {
            Logger.info("No Zephyr tags found");
        } else {
            Logger.info("Publishing results to Zephyr");
            try {
                Zapi.createNewTestCycle(TEST_PLAN_NAME);
                Zapi.addTestsToCycle(results.stream().map(TestResult::getIssueNumber).collect(Collectors.toList()));
                Zapi.executeTests(results);
            } catch (Exception e) {
                Logger.err("Zephyr results publishing has been failed");
            }
        }
    }
}
