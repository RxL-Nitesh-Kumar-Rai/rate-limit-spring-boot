package blue.optima.assignment.cache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;

public class HazlecastClientConfig {
    @Bean
    public HazelcastInstance hazelcastInstance(ClientConfig clientConfig) {
        return HazelcastClient.newHazelcastClient(clientConfig);
    }

    @Bean
    public ClientConfig clientConfig() {
        ClientConfig clientConfig = new ClientConfig();
//        GroupConfig groupConfig = config.getGroupConfig();
//        groupConfig.setName("dev");
//        groupConfig.setPassword("dev-pass");
        return clientConfig;
    }
}
