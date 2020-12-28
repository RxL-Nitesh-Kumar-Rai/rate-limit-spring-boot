package blue.optima.assignment.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import blue.optima.assignment.model.ThrottlingConfiguration;
import blue.optima.assignment.persistence.RuleConfigurerPersistenceService;
import blue.optima.assignment.scheduler.RateLimiterInfo;
import blue.optima.assignment.scheduler.Scheduler;

public class BlueOptimaRateLimiterConfigrationFetcher implements Runnable {

	private final RuleConfigurerPersistenceService ruleConfigurerPersistenceService;

	private final String moduleName;

	private final Scheduler scheduler;

	private final Map<String, ThrottlingConfiguration> mappingOfKeyWithThrottlingLimit;

	private final Map<ThrottlingConfiguration, ScheduledFuture<?>> mapOfTasks;

	public BlueOptimaRateLimiterConfigrationFetcher(final RuleConfigurerPersistenceService ruleConfigurerPersistenceService, final String moduleName, final Scheduler scheduler, final RateLimiterInfo rateLimiterInfo) {
		this.ruleConfigurerPersistenceService = ruleConfigurerPersistenceService;
		this.moduleName = moduleName;
		mappingOfKeyWithThrottlingLimit = rateLimiterInfo.getMappingOfKeyWithThrottlingLimit();
		mapOfTasks = rateLimiterInfo.getMapOfTasks();
		this.scheduler = scheduler;
	}

	@Override
	public void run() {

		//add the newely added keys.
		List<ThrottlingConfiguration> listOfActiveThrottlingConfiguration = ruleConfigurerPersistenceService.getAllActiveCheckPointsInModule(moduleName);
		for (final ThrottlingConfiguration config : listOfActiveThrottlingConfiguration) {
			final String key = scheduler.createKey(config);
			if (!mappingOfKeyWithThrottlingLimit.containsKey(key)) {
				mappingOfKeyWithThrottlingLimit.put(key, config);
			}
		}

		//remove the recently remove keys.
		listOfActiveThrottlingConfiguration = ruleConfigurerPersistenceService.getAllInActiveCheckPointsInModule(moduleName);
		for (final ThrottlingConfiguration config : listOfActiveThrottlingConfiguration) {
			final String key = scheduler.createKey(config);
			if (mappingOfKeyWithThrottlingLimit.containsKey(key)) {
				mappingOfKeyWithThrottlingLimit.remove(key);
			}
		}

	}

}
