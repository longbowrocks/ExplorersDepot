package bike.guyona.exdepot.sortingrules;

public class RuleUIDMapException extends Exception {
    private String details;

    public RuleUIDMapException(String details) {
        details = details;
    }

    public String getDetails() {
        return details;
    }
}
