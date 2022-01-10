package blue.optima.assignment.apis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import blue.optima.assignment.cache.CacheService;


@Service("rateLimiterImpl")
public class RateLimiterImpl implements RateLimiter {

	private final String moduleName;

	private final CacheService<String, Integer> cacheService;

	@Autowired
	public RateLimiterImpl(@Value("${blue.optima.rate.limter.module.name}") final String moduleName, @Qualifier("cacheService") final CacheService<String, Integer> cacheService) {
		this.cacheService = cacheService;
		this.moduleName = moduleName;
	}

	@Override
	public boolean allowRequest(final String userName, final String httpFunc, final String api) {
		
		// Please fill in the code.
		
		return Boolean.TRUE;
	}

}
