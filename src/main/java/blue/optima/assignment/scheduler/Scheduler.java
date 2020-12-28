package blue.optima.assignment.scheduler;

import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import blue.optima.assignment.cache.CacheService;
import blue.optima.assignment.model.ThrottlingConfiguration;
import blue.optima.assignment.persistence.RuleConfigurerPersistenceService;
import blue.optima.assignment.task.BlueOptimaRateLimiterConfigrationFetcher;
import blue.optima.assignment.task.BlueOptimaRateLimiterRefillTask;

@Component("scheduler")
public class Scheduler {

	@Autowired
	private RuleConfigurerPersistenceService ruleConfigurerPersistenceService;

	@Autowired
	private CacheService<String, Integer> cacheService;

	@Value("${blue.optima.rate.limter.module.name}")
	private String moduleName;

	private boolean isInitDone;

	@Autowired
	private RateLimiterInfo rateLimiterInfo;

	ScheduledExecutorService execService = Executors.newScheduledThreadPool(1);

	ScheduledExecutorService execService4Refill = Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors());

	//final RuleConfigurerPersistenceService ruleConfigurerPersistenceService, final String moduleName, final Map<String, ThrottlingConfiguration> mappingOfKeyWithThrottlingLimit, final Scheduler scheduler
	public void scheduleKeyUpdater() {
		execService.scheduleAtFixedRate(new BlueOptimaRateLimiterConfigrationFetcher(ruleConfigurerPersistenceService, moduleName, this, rateLimiterInfo), 60, 60, TimeUnit.SECONDS);
	}

	public ScheduledFuture<?> scheduleNewTask(final ThrottlingConfiguration config) {
		ScheduledFuture<?> future = null;
		System.out.println("The output is config.getTime_limit() " + config.getTime_limit() + " config " + config);
		future = execService4Refill.scheduleAtFixedRate(new BlueOptimaRateLimiterRefillTask(createKey(config), config, cacheService), config.getTime_limit(), config.getTime_limit(), TimeUnit.SECONDS);
		rateLimiterInfo.getMapOfTasks().put(config, future);
		return future;
	}

	public boolean isInitDone() {
		return isInitDone;
	}

	public RateLimiterInfo getInitRateLimiterInfo() {
		return rateLimiterInfo;
	}

	@PostConstruct
	public RateLimiterInfo init() {
		ScheduledFuture<?> future = null;
		final List<ThrottlingConfiguration> listOfThrottlingConfiguration = ruleConfigurerPersistenceService.getAllActiveCheckPointsInModule(moduleName);

		for (final ThrottlingConfiguration config : listOfThrottlingConfiguration) {
			rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().put(createKeyWithTimeLimit(config), config);
			cacheService.add(createKeyWithTimeLimit(config), config.getThrottle_limit(), config.getTime_limit());
			//Refill will happen here.
			future = execService4Refill.scheduleAtFixedRate(new BlueOptimaRateLimiterRefillTask(createKeyWithTimeLimit(config), config, cacheService), config.getTime_limit(), config.getTime_limit(), TimeUnit.SECONDS);
			rateLimiterInfo.getMapOfTasks().put(config, future);
		}

		isInitDone = true;
		return rateLimiterInfo;
	}

	public ThrottlingConfiguration getConfiguration(final String key) {
		return rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().get(key);
	}

	public Set<String> getAllKeysContainingAString(final String key) {
		return getByPrefix(rateLimiterInfo.getMappingOfKeyWithThrottlingLimit(), key).keySet();
	}

	private SortedMap<String, ThrottlingConfiguration> getByPrefix(final NavigableMap<String, ThrottlingConfiguration> map, final String prefix) {
		return map.subMap(prefix, prefix + Character.MAX_VALUE);
	}

	//  /v1/giftcards or /v2/internal/test/api
	private String convertUriIntoKey(final String uri) {
		return uri.replace("/", "-");
	}

	public String createKey(final String userName, final String httpFunc, final String api) {
		return new StringBuilder(userName).append("-").append(httpFunc).append("-").append(convertUriIntoKey(api)).toString();

	}

	public String createKey(final ThrottlingConfiguration throttlingConfiguration) {
		return new StringBuilder(throttlingConfiguration.getUsername()).append("-").append(throttlingConfiguration.getHttp_func()).append("-").append(convertUriIntoKey(throttlingConfiguration.getUri())).toString();
	}

	public String createKeyWithTimeLimit(final ThrottlingConfiguration throttlingConfiguration) {
		return new StringBuilder(throttlingConfiguration.getUsername()).append("-").append(throttlingConfiguration.getHttp_func()).append("-").append(convertUriIntoKey(throttlingConfiguration.getUri()))
				.append(Integer.valueOf(throttlingConfiguration.getTime_limit())).toString();
	}

}
