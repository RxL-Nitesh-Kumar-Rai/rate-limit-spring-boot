package blue.optima.assignment.model;

public class ThrottlingConfiguration {

	private final String username;

	private final String http_func;

	private final String uri;

	private final String module;

	private final Integer throttle_limit;

	private final Integer time_limit;

	private final String status;

	private final String updated_on;

	public ThrottlingConfiguration(final String username, final String http_func, final String uri, final String module, final Integer throttle_limit, final Integer time_limit, final String updated_on, final String status) {
		this.username = username;
		this.uri = uri;
		this.module = module;
		this.throttle_limit = throttle_limit;
		this.http_func = http_func;
		this.time_limit = time_limit;
		this.status = status;
		this.updated_on = updated_on;

	}

	public ThrottlingConfiguration() {
		this(null, null, null, null, null, null, null, null);

	}

	public String getUpdated_on() {
		return updated_on;
	}

	public String getModule() {
		return module;
	}

	public String getUsername() {
		return username;
	}

	public String getHttp_func() {
		return http_func;
	}

	public String getUri() {
		return uri;
	}

	public Integer getThrottle_limit() {
		return throttle_limit;
	}

	public Integer getTime_limit() {
		return time_limit;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (http_func == null ? 0 : http_func.hashCode());
		result = prime * result + (module == null ? 0 : module.hashCode());
		result = prime * result + (status == null ? 0 : status.hashCode());
		result = prime * result + (throttle_limit == null ? 0 : throttle_limit.hashCode());
		result = prime * result + (time_limit == null ? 0 : time_limit.hashCode());
		result = prime * result + (uri == null ? 0 : uri.hashCode());
		result = prime * result + (username == null ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ThrottlingConfiguration other = (ThrottlingConfiguration) obj;
		if (http_func == null) {
			if (other.http_func != null) {
				return false;
			}
		} else if (!http_func.equals(other.http_func)) {
			return false;
		}
		if (module == null) {
			if (other.module != null) {
				return false;
			}
		} else if (!module.equals(other.module)) {
			return false;
		}
		if (status == null) {
			if (other.status != null) {
				return false;
			}
		} else if (!status.equals(other.status)) {
			return false;
		}
		if (throttle_limit == null) {
			if (other.throttle_limit != null) {
				return false;
			}
		} else if (!throttle_limit.equals(other.throttle_limit)) {
			return false;
		}
		if (time_limit == null) {
			if (other.time_limit != null) {
				return false;
			}
		} else if (!time_limit.equals(other.time_limit)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ThrottlingConfiguration [username=" + username + ", http_func=" + http_func + ", uri=" + uri + ", module=" + module + ", throttle_limit=" + throttle_limit + ", time_limit=" + time_limit + ", status=" + status + "]";
	}

}
