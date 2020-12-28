package blue.optima.assignment.cache;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static redis.clients.util.Sharded.DEFAULT_WEIGHT;

import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

@Service("cacheService")
public class CacheServiceImpl implements CacheService<String, Integer> {

	@Value("${cache.expiration}")
	private int cacheExpiration;

	@Value("${redis.servers}")
	private String servers;

	@Value("${redis.conf.maxTotal}")
	private int maxTotal;

	@Value("${redis.conf.maxIdle}")
	private int maxIdle;

	@Value("${redis.conf.minIdle}")
	private int minIdle;

	@Value("${redis.conf.maxWaitMillis}")
	private int maxWaitMillis;

	@Value("${redis.conf.testWhileIdle}")
	private boolean testWhileIdle;

	@Value("${redis.conf.testOnReturn}")
	private boolean testOnReturn;

	@Value("${redis.conf.minEvictableIdleTimeMillis}")
	private int minEvictableIdleTimeMillis;

	@Value("${redis.conf.timeBetweenEvictionRunsMillis}")
	private int timeBetweenEvictionRunsMillis;

	@Value("${redis.conf.numTestsPerEvictionRun}")
	private int numTestsPerEvictionRun;

	@Value("${redis.shards.timeout}")
	private int timeOut;

	@Value("${redis.shards.socket.timeout}")
	private int socketTimeOut;

	private ShardedJedisPool pool;

	@PostConstruct
	public void initIt() throws Exception {
		try {
			final HashSet<HostAndPort> hostPortSet = getHostAndPortObjectFromString(servers);
			final List<JedisShardInfo> shards = newArrayList();
			hostPortSet.forEach(e -> {
				shards.add(new JedisShardInfo(e.getHost(), e.getPort(), timeOut, socketTimeOut, DEFAULT_WEIGHT));
			});

			final JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxTotal(maxTotal);
			config.setMaxIdle(maxIdle);
			config.setMinIdle(minIdle);
			config.setMaxWaitMillis(maxWaitMillis);
			config.setTestWhileIdle(testWhileIdle);
			config.setTestOnReturn(testOnReturn);
			config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
			config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
			config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
			System.out.println("shards = " + shards + "," + "config = " + config);

			pool = new ShardedJedisPool(config, shards);
		} catch (final Exception e) {
			System.out.println("Error creating RedisCluster " + e);
		}
	}

	@PreDestroy
	public void cleanUp() {
		if (null != pool) {
			try {
				pool.close();
			} catch (final Exception e) {
				System.out.println("could not close redis shard pool." + e);
			}
		}
	}

	@Override
	public <T> T get(final String key, final Class<T> t) {
		try (final ShardedJedis jedis = pool.getResource();) {
			//			final String key = createCacheKey(k, prefix);
			final String json = jedis.get(key);
			System.out.println("key = " + key + ", json = " + json);
			if (isNotEmpty(json)) {
				final Optional<T> obj = serialzer.toObject(json, t);
				if (obj.isPresent()) {
					return obj.get();
				}
			}
			return null;
		}
	}

	@Override
	public void add(final String k, final Object v) {
		add(k, v, -1);
	}

	@Override
	public void add(final String key, final Object v, final int expiration) {
		try (final ShardedJedis jedis = pool.getResource();) {
			final Optional<String> jsonStr = serialzer.toJson(v);
			if (jsonStr.isPresent()) {
				//				final String key = createCacheKey(k, prefix);
				jedis.set(key, jsonStr.get());
				jedis.expire(key, expiration > 0 ? expiration : cacheExpiration);
			}
			System.out.println("key = " + key + "object = " + v + ", jsonStr = " + jsonStr.get() + ", cacheExpiration = " + cacheExpiration);
		}
	}

	@Override
	public Long incr(final String key, final Integer expiration) {
		try (final ShardedJedis jedis = pool.getResource();) {
			//			final String key = createCacheKey(k, prefix);
			final Long i = jedis.incr(key);
			// set the expiration at first
			if (null != i && i <= 1) {
				jedis.expire(key, expiration);
			}
			return i;
		}
	}

	@Override
	public Long incr(final String key) {
		try (final ShardedJedis jedis = pool.getResource();) {
			//			final String key = createCacheKey(k, prefix);
			final Long i = jedis.incr(key);
			return i;
		}
	}

	@Override
	public Long decrease(final String key, final Integer expiration) {
		try (final ShardedJedis jedis = pool.getResource();) {
			//			final String key = createCacheKey(k, prefix);
			final Long i = jedis.decr(key);
			// set the expiration at first
			if (null != i && i <= 1) {
				jedis.expire(key, expiration);
			}
			return i;
		}
	}

	@Override
	public Long decrease(final String key) {
		try (final ShardedJedis jedis = pool.getResource();) {
			final Long i = jedis.decr(key);
			return i;
		}
	}

	@Override
	public Long delete(final String key) {
		try (final ShardedJedis jedis = pool.getResource();) {
			//			final String key = createCacheKey(k, prefix);
			return jedis.del(key);
		}
	}
}
