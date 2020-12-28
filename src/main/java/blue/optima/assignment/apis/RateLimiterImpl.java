package blue.optima.assignment.apis;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import blue.optima.assignment.cache.CacheService;
import blue.optima.assignment.model.ThrottlingConfiguration;
import blue.optima.assignment.scheduler.Scheduler;

@Service("rateLimiterImpl")
public class RateLimiterImpl implements RateLimiter {

	private final String moduleName;

	private final CacheService<String, Integer> cacheService;

	private final Scheduler scheduler;

	@Autowired
	public RateLimiterImpl(@Value("${blue.optima.rate.limter.module.name}") final String moduleName, @Qualifier("cacheService") final CacheService<String, Integer> cacheService, @Qualifier("scheduler") final Scheduler scheduler) {
		this.scheduler = scheduler;
		this.cacheService = cacheService;
		this.moduleName = moduleName;
	}

	@Override
	public boolean allowRequest(final String userName, final String httpFunc, final String api) {
		final String key = scheduler.createKey(userName, httpFunc, api);

		final Set<String> listOfKeys = scheduler.getAllKeysContainingAString(key);

		if (listOfKeys.size() == 0) {
			return Boolean.TRUE;
		}

		for (final String keys : listOfKeys) {
			final Integer cachedValue = cacheService.get(key, Integer.class);
			if (cachedValue == null) {
				final ThrottlingConfiguration throttlingConfiguration = scheduler.getConfiguration(keys);
				cacheService.add(keys, throttlingConfiguration.getThrottle_limit(), throttlingConfiguration.getTime_limit());
				continue;
			}
			if (cachedValue < 0) {
				return Boolean.FALSE;
			} else {
				cacheService.decrease(key);
				continue;
			}
		}
		return Boolean.TRUE;
	}

}
