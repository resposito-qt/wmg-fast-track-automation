package framework.platform.html;

public enum SelectorType {

    CSS("css"),
    XPATH("xpath");

    private final String type;

    SelectorType(String s) {
        type = s;
    }

    public String getValue() {
        return type;
    }
}
