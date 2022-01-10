package blue.optima.assignment.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.grid.GridBucketState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("hazelcastService")
public class HazelcastService {

    private final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config1());
//    @Autowired
//    @Qualifier("hazelcastInstance")
//    HazelcastInstance hazelcastInstance;



    public IMap<String, GridBucketState> getCache(String cacheName) {
        return hazelcastInstance.getMap(cacheName);
    }
    private Config config1(){
        Config config = new Config();
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(true)
                .getMembers()
                .add("192.168.1.11");
        return config;
    }
    private Config config2(){
        Config config = new Config();
        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortCount(20);
        network.setPortAutoIncrement(false);
        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig()
                .addMember("machine1")
                .addMember("localhost").setEnabled(true);
        return config;
    }
}
