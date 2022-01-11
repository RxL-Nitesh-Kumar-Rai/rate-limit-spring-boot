package blue.optima.assignment.model;

public class MappingTable {
    private final String userName;
    private final String api;
    private final Integer rateLimit;

    public MappingTable(String userName, String api, Integer rateLimit) {
        this.userName = userName;
        this.api = api;
        this.rateLimit = rateLimit;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getApi() {
        return this.api;
    }

    public Integer getRateLimit() {
        return this.rateLimit;
    }
}
