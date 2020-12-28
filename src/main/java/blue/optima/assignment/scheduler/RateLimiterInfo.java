package blue.optima.assignment.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.stereotype.Service;

import blue.optima.assignment.model.ThrottlingConfiguration;

@Service("rateLimiterInfo")
public class RateLimiterInfo {

	Map<ThrottlingConfiguration, ScheduledFuture<?>> mapOfTasks = new HashMap<>();

	TreeMap<String, ThrottlingConfiguration> mappingOfKeyWithThrottlingLimit = new TreeMap<>();

	public Map<ThrottlingConfiguration, ScheduledFuture<?>> getMapOfTasks() {
		return mapOfTasks;
	}

	public TreeMap<String, ThrottlingConfiguration> getMappingOfKeyWithThrottlingLimit() {
		return mappingOfKeyWithThrottlingLimit;
	}

}
