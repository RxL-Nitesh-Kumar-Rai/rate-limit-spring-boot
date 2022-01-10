package blue.optima.assignment.cache;

//import com.hazelcast.core.Hazelcast;
import com.hazelcast.map.IMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.RecoveryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import io.github.bucket4j.grid.hazelcast.Hazelcast;

import java.time.Duration;
import java.util.Map;

@Service("hazCacheService")
public class HazCacheService {
    @Autowired
    private Environment env;

    @Autowired
    private HazelcastService hazelcastService;

//    public IMap<String, GridBucketState> cache = hazelcastService.getCache("bucketCache");
    public IMap<String, GridBucketState> getCache() {
        return hazelcastService.getCache("bucketCache");
    }
    public Bucket resolveBucket(String apiKey, Integer capacity) {
        return newBucket(capacity, getCache(), apiKey);
    }

    public Bucket updateBucket(String apiKey, Integer capacity) {
        IMap<String, GridBucketState> iMap = getCache();
        iMap.remove(apiKey);
        return newBucket(capacity, iMap, apiKey);
    }



    private Bucket newBucket(Integer capacity, IMap<String, GridBucketState> map, String apiKey) {
        String timeUnit = env.getProperty("rate.limit.time.unit");
        switch (timeUnit) {
            case "SECONDS":
                return Bucket4j.extension(Hazelcast.class).builder()
                        .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofSeconds(10))))
                        .build(map, apiKey, RecoveryStrategy.RECONSTRUCT);
            case "MINUTES":
                return Bucket4j.extension(Hazelcast.class).builder()
                        .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(1))))
                        .build(map, apiKey, RecoveryStrategy.RECONSTRUCT);
            case "HOURS":
                return Bucket4j.extension(Hazelcast.class).builder()
                        .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofHours(1))))
                        .build(map, apiKey, RecoveryStrategy.RECONSTRUCT);
            default:
                return Bucket4j.extension(Hazelcast.class).builder()
                        .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(1))))
                        .build(map, apiKey, RecoveryStrategy.RECONSTRUCT);
        }
    }

}
