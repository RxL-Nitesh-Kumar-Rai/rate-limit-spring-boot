package blue.optima.assignment.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.grid.GridBucketState;
import org.springframework.stereotype.Service;

@Service("hazelcastService")
public class HazelcastService {

    private final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config1());
    public IMap<String, GridBucketState> getCache(String cacheName) {
        return hazelcastInstance.getMap(cacheName);
    }
    private Config config1(){
        Config config = new Config();
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(true)
                .getMembers()
                .add("localhost");
        return config;
    }
}
