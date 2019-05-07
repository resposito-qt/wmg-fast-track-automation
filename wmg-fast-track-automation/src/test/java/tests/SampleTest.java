package tests;

import framework.zapi.Zephyr;
import org.testng.annotations.Test;


/**
 * SampleTest
 */
public class SampleTest {

    @Test(groups = "integration") // use custom groups to run the single test locally
    @Zephyr(key = "AS-3732") // jira issue number for Zephyr reporter
    public void sampleTestCheck() {
        
    }
}
