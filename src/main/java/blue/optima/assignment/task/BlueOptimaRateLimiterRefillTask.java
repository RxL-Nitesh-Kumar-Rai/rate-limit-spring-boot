package blue.optima.assignment.task;

import blue.optima.assignment.cache.CacheService;
import blue.optima.assignment.model.ThrottlingConfiguration;

public class BlueOptimaRateLimiterRefillTask implements Runnable {

	private final ThrottlingConfiguration throttlingConfiguration;
	private final String key;
	private final CacheService<String, Integer> cacheService;

	public BlueOptimaRateLimiterRefillTask(final String key, final ThrottlingConfiguration throttlingConfiguration, final CacheService<String, Integer> cacheService) {
		this.throttlingConfiguration = throttlingConfiguration;
		this.cacheService = cacheService;
		this.key = key;
	}

	@Override
	public void run() {
		System.out.println(" --- BlueOptimaRateLimiterRefillTask :: key " + key + " throttling limit " + throttlingConfiguration.getThrottle_limit() + " time limit " + throttlingConfiguration.getTime_limit());
		cacheService.add(key, throttlingConfiguration.getThrottle_limit(), throttlingConfiguration.getTime_limit());
	}
}
