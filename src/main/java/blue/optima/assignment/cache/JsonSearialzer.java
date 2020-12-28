package blue.optima.assignment.cache;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

@Service
public final class JsonSearialzer {

	public final ObjectMapper mapper = new ObjectMapper();

	public Optional<String> toJson(final Object v) {
		try {
			return fromNullable(mapper.writeValueAsString(v));
		} catch (final Exception e) {
			System.out.println("Exception while converting to json : " + e);
		}
		return absent();
	}

	public <T> Optional<T> toObject(final String v, final Class<T> t) {
		try {
			return fromNullable(mapper.readValue(v, t));
		} catch (final Exception e) {
			System.out.println("Exception while converting to Object : " + e);
		}
		return absent();
	}
}
