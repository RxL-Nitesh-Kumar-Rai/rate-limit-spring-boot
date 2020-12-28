package blue.optima.assignment;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import blue.optima.assignment.model.ThrottlingConfiguration;
import blue.optima.assignment.persistence.RuleConfigurerPersistenceService;
import blue.optima.assignment.scheduler.RateLimiterInfo;
import blue.optima.assignment.scheduler.Scheduler;

@SpringBootApplication
public class RateLimiterApplication implements CommandLineRunner {

	@Autowired
	private Scheduler scheduler;

	@Value("${blue.optima.rate.limter.module.name}")
	private String moduleName;

	@Autowired
	private RuleConfigurerPersistenceService ruleConfigurerPersistenceService;

	public static void main(final String[] args) {
		SpringApplication.run(RateLimiterApplication.class, args);
	}

	@Override
	public void run(final String... args) throws Exception {
		final RateLimiterInfo rateLimiterInfo = scheduler.isInitDone() ? scheduler.getInitRateLimiterInfo() : scheduler.init();

		while (true) {
			List<ThrottlingConfiguration> listOfActiveThrottlingConfiguration = ruleConfigurerPersistenceService.getAllActiveCheckPointsInModule(moduleName);
			addRecentlyAddedKeys(listOfActiveThrottlingConfiguration, rateLimiterInfo);
			listOfActiveThrottlingConfiguration = ruleConfigurerPersistenceService.getAllInActiveCheckPointsInModule(moduleName);
			removeInActiveKeys(listOfActiveThrottlingConfiguration, rateLimiterInfo);
			Thread.sleep(1000 * 100);
		}
	}

	//Remove the recently removed keys from rate limiting configuration.
	private void removeInActiveKeys(final List<ThrottlingConfiguration> listThrottlingConfiguration, final RateLimiterInfo rateLimiterInfo) {
		for (final ThrottlingConfiguration config : listThrottlingConfiguration) {
			final String key = scheduler.createKeyWithTimeLimit(config);
			if (rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().containsKey(key) && rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().get(key).getStatus().equalsIgnoreCase("inactive")) {
				rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().remove(key);
				rateLimiterInfo.getMapOfTasks().get(config).cancel(true);
			}
		}
	}

	//Add the recently added keys from the rate limiting configuration.
	private void addRecentlyAddedKeys(final List<ThrottlingConfiguration> listThrottlingConfiguration, final RateLimiterInfo rateLimiterInfo) {
		for (final ThrottlingConfiguration config : listThrottlingConfiguration) {
			final String key = scheduler.createKeyWithTimeLimit(config);
			//final String key = scheduler.createKey(config);
			if (!rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().containsKey(key)) {
				rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().put(key, config);
				rateLimiterInfo.getMapOfTasks().put(config, scheduler.scheduleNewTask(config));
				continue;
			}

			//If there is an update in the rate limit configuration.
			if (config.getUpdated_on() != null && !rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().get(key).getUpdated_on().equals(config.getUpdated_on())) {
				//Remove the existing entry from scheduler.
				rateLimiterInfo.getMapOfTasks().get(rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().get(key)).cancel(true);
				rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().remove(key);

				//Add the updated entry.
				rateLimiterInfo.getMappingOfKeyWithThrottlingLimit().put(key, config);
				rateLimiterInfo.getMapOfTasks().put(config, scheduler.scheduleNewTask(config));
			}
		}
	}

}
