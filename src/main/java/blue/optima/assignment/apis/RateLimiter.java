package blue.optima.assignment.apis;

public interface RateLimiter {

	boolean allowRequest(final String userName, final String httpFunc, final String api);

}
