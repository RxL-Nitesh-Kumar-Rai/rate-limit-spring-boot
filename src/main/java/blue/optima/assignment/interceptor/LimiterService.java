package blue.optima.assignment.interceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service("limiterService")
public class LimiterService {
    @Autowired
    private Environment env;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String apiKey, Integer capacity) {
        return cache.computeIfAbsent(apiKey, (x) -> newBucket(capacity));
    }

    public Bucket updateBucket(String apiKey, Integer capacity) {
        return cache.compute(apiKey, (x, y) -> newBucket(capacity));
    }


    public Bucket newBucket(Integer capacity) {
        String timeUnit = env.getProperty("rate.limit.time.unit");
        switch (timeUnit) {
            case "SECONDS":
                return Bucket4j.builder()
                        .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofSeconds(10))))
                        .build();
            case "MINUTES":
                return Bucket4j.builder()
                        .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(1))))
                        .build();
            case "HOURS":
                return Bucket4j.builder()
                        .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofHours(1))))
                        .build();
            default:
                return Bucket4j.builder()
                        .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(1))))
                        .build();
        }
    }
}