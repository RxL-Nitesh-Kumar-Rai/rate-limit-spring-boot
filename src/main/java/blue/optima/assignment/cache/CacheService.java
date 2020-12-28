package blue.optima.assignment.cache;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Integer.valueOf;
import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.List;

import redis.clients.jedis.HostAndPort;

public interface CacheService<K, T> {

	String AT = "@";
	JsonSearialzer serialzer = new JsonSearialzer();

	default HashSet<HostAndPort> getHostAndPortObjectFromString(final String ser) {
		final List<String> list = asList(ser.split(" "));
		final HashSet<HostAndPort> hostPortSet = newHashSet();
		list.forEach(e -> {
			final String[] s = e.split(":");
			hostPortSet.add(new HostAndPort(s[0], valueOf(s[1])));
		});
		return hostPortSet;
	}

	<T> T get(final String K, Class<T> T);

	void add(final String K, final Object V);

	void add(final String K, final Object V, final int expiration);

	Long incr(final String K, Integer expiration);

	Long decrease(final String K, Integer expiration);

	Long delete(final String k);

	Long incr(String key);

	Long decrease(String key);

}
