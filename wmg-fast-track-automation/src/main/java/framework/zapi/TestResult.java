package framework.zapi;


public class TestResult {

    private String issueNumber;
    private int resultStatus;

    public TestResult(String issueNumber, int resultStatus) {
        this.issueNumber = issueNumber;
        this.resultStatus = resultStatus;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

    public int getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(int resultStatus) {
        this.resultStatus = resultStatus;
    }
}
