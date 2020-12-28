package blue.optima.assignment.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import blue.optima.assignment.model.ThrottlingConfiguration;
import blue.optima.assignment.model.ThrottlingConfigurationOrmObj;

@Service("ruleConfigurerPersistenceService")
public class RuleConfigurerPersistenceService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static String create = "insert into ratelimiter.configuration(username,http_func,uri,module,throttle_limit,time_limit,status) values(?,?,?,?,?,?,?)";
	private static String update = "update ratelimiter.configuration set username = ?,http_func = ?,uri = ?,module = ?,throttle_limit = ?,time_limit = ?,status = ? where id = ? ";

	private static String updateStatus = "update ratelimiter.configuration set status=? where id=?";
	private static String getEntry4Url = "select * from ratelimiter.configuration where uri = ? and http_func = ? and username = ? and module = ?";
	private static String updateThrollingLimit = "update ratelimiter.configuration set throttle_limit = ?,time_limit = ? where uri = ? and username = ? and module = ?";
	private static String getAllActiveCheckPoints = "select * from ratelimiter.configuration where status=? and username = ?";
	private static String getAllActiveCheckPoints4UserInModule = "select * from ratelimiter.configuration where status=? and username = ? and module = ?";
	private static String getAllActiveCheckPointsInModule = "select * from ratelimiter.configuration where status=? and module = ?";

	private static String updateStatusOfUserForAllUrls = "update ratelimiter.configuration set status = ? where username = ?";

	public void updateStatus(final String status, final ThrottlingConfiguration throttlingConfiguration) {
		final Optional<ThrottlingConfigurationOrmObj> optionalExistingObj = getEntry4Uri(throttlingConfiguration.getUri(), throttlingConfiguration.getHttp_func(), throttlingConfiguration.getUsername(), throttlingConfiguration.getModule());

		if (optionalExistingObj.isPresent()) {
			final ThrottlingConfigurationOrmObj existingObj = optionalExistingObj.get();
			if (existingObj.getUsername().equals(throttlingConfiguration.getUsername()) && existingObj.getUri().equals(throttlingConfiguration.getUri()) && existingObj.getStatus().equalsIgnoreCase(throttlingConfiguration.getStatus())
					&& existingObj.getHttp_func().equalsIgnoreCase(throttlingConfiguration.getHttp_func()) && existingObj.getTime_limit() == throttlingConfiguration.getTime_limit()
					&& existingObj.getThrottle_limit() == throttlingConfiguration.getThrottle_limit() && existingObj.getModule().equalsIgnoreCase(throttlingConfiguration.getModule())) {
				return;
			}

			if (existingObj != null) {
				jdbcTemplate.update(updateStatus, status, existingObj.getId());
				return;
			}
		}
		jdbcTemplate.update(create, throttlingConfiguration.getUsername(), throttlingConfiguration.getHttp_func(), throttlingConfiguration.getUri(), throttlingConfiguration.getThrottle_limit(), throttlingConfiguration.getTime_limit());
	}

	public int update(final ThrottlingConfiguration throttlingConfiguration) {
		final Optional<ThrottlingConfigurationOrmObj> optionalExistingObj = getEntry4Uri(throttlingConfiguration.getUri(), throttlingConfiguration.getHttp_func(), throttlingConfiguration.getUsername(), throttlingConfiguration.getModule());

		if (optionalExistingObj.isPresent()) {
			final ThrottlingConfigurationOrmObj existingObj = optionalExistingObj.get();
			if (existingObj.getUsername().equals(throttlingConfiguration.getUsername()) && existingObj.getUri().equals(throttlingConfiguration.getUri()) && existingObj.getStatus().equalsIgnoreCase(throttlingConfiguration.getStatus())
					&& existingObj.getHttp_func().equalsIgnoreCase(throttlingConfiguration.getHttp_func()) && existingObj.getTime_limit() == throttlingConfiguration.getTime_limit()
					&& existingObj.getThrottle_limit() == throttlingConfiguration.getThrottle_limit() && existingObj.getModule().equalsIgnoreCase(throttlingConfiguration.getModule())) {
				return 0;
			}

			if (existingObj != null) {
				return jdbcTemplate.update(update, throttlingConfiguration.getUsername(), throttlingConfiguration.getHttp_func(), throttlingConfiguration.getUri(), throttlingConfiguration.getModule(), throttlingConfiguration.getThrottle_limit(),
						throttlingConfiguration.getTime_limit(), throttlingConfiguration.getStatus(), existingObj.getId());
			}
		}
		final int queryStatus = jdbcTemplate.update(create, throttlingConfiguration.getUsername(), throttlingConfiguration.getHttp_func(), throttlingConfiguration.getUri(), throttlingConfiguration.getModule(), throttlingConfiguration.getThrottle_limit(),
				throttlingConfiguration.getTime_limit(), "active");

		return queryStatus;

	}

	//This will always create a new object in the DB.If it is not present.Otherwise..if the object is present and it is different then the existing one
	//then mark it inactive and make a new entry.
	public int create(final ThrottlingConfiguration throttlingConfiguration) {
		final Optional<ThrottlingConfigurationOrmObj> optionalExistingObj = getEntry4Uri(throttlingConfiguration.getUri(), throttlingConfiguration.getHttp_func(), throttlingConfiguration.getUsername(), throttlingConfiguration.getModule());

		if (optionalExistingObj.isPresent()) {
			final ThrottlingConfigurationOrmObj existingObj = optionalExistingObj.get();
			if (existingObj.getUsername().equals(throttlingConfiguration.getUsername()) && existingObj.getUri().equals(throttlingConfiguration.getUri()) && existingObj.getStatus().equalsIgnoreCase(throttlingConfiguration.getStatus())
					&& existingObj.getHttp_func().equalsIgnoreCase(throttlingConfiguration.getHttp_func()) && existingObj.getTime_limit() == throttlingConfiguration.getTime_limit()
					&& existingObj.getThrottle_limit() == throttlingConfiguration.getThrottle_limit() && existingObj.getModule().equalsIgnoreCase(throttlingConfiguration.getModule())) {
				return 0;
			}

			if (existingObj != null) {
				jdbcTemplate.update(updateStatus, "inactive", existingObj.getId());
			}
		}
		final int queryStatus = jdbcTemplate.update(create, throttlingConfiguration.getUsername(), throttlingConfiguration.getHttp_func(), throttlingConfiguration.getUri(), throttlingConfiguration.getModule(), throttlingConfiguration.getThrottle_limit(),
				throttlingConfiguration.getTime_limit(), "active");

		return queryStatus;

	}

	public Optional<ThrottlingConfigurationOrmObj> getEntry4Uri(final String uri, final String httpFunc, final String username, final String module) {
		final List<ThrottlingConfigurationOrmObj> listOfObj = jdbcTemplate.query(getEntry4Url, new ThrottlingConfigurationOrmObjRowMapper(), uri, httpFunc, username, module);
		if (listOfObj == null || listOfObj.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(listOfObj.get(0));
	}

	public void updateThrottlingLimit(final String uri, final String userName, final int throttlingLimit, final int thottlingTime, final String module) {
		jdbcTemplate.update(updateThrollingLimit, throttlingLimit, thottlingTime, uri, userName, module);
	}

	public void updateStatusForUser(final String status, final String userName) {
		jdbcTemplate.update(updateStatusOfUserForAllUrls, status, userName);
	}

	public List<ThrottlingConfiguration> getAllActiveCheckPointsOfUser(final String userName) {
		return jdbcTemplate.query(getAllActiveCheckPoints, new ThrottlingConfigurationRowMapper(), new Object[] { "active", userName });
	}

	public List<ThrottlingConfiguration> getAllActiveCheckPointsOfUserInModule(final String userName, final String module) {
		return jdbcTemplate.query(getAllActiveCheckPoints4UserInModule, new ThrottlingConfigurationRowMapper(), new Object[] { "active", userName, module });
	}

	public List<ThrottlingConfiguration> getAllActiveCheckPointsInModule(final String moduleName) {
		return jdbcTemplate.query(getAllActiveCheckPointsInModule, new ThrottlingConfigurationRowMapper(), new Object[] { "active", moduleName });
	}

	public List<ThrottlingConfiguration> getAllInActiveCheckPointsInModule(final String moduleName) {
		return jdbcTemplate.query(getAllActiveCheckPointsInModule, new ThrottlingConfigurationRowMapper(), new Object[] { "inactive", moduleName });
	}

	public Optional<ThrottlingConfiguration> deleteConfiguration(final String uri, final String username) {

		return Optional.empty();

	}
}

class ThrottlingConfigurationRowMapper implements RowMapper<ThrottlingConfiguration> {
	@Override
	public ThrottlingConfiguration mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		final ThrottlingConfiguration obj = new ThrottlingConfiguration(rs.getString("username"), rs.getString("http_func"), rs.getString("uri"), rs.getString("module"), rs.getInt("throttle_limit"), rs.getInt("time_limit"), rs.getString("update_on"),
				rs.getString("status"));
		return obj;
	}
}

class ThrottlingConfigurationOrmObjRowMapper implements RowMapper<ThrottlingConfigurationOrmObj> {
	@Override
	public ThrottlingConfigurationOrmObj mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		final ThrottlingConfigurationOrmObj obj = new ThrottlingConfigurationOrmObj(rs.getInt("id"), rs.getString("username"), rs.getString("http_func"), rs.getString("uri"), rs.getString("module"), rs.getInt("throttle_limit"), rs.getInt("time_limit"),
				rs.getString("update_on"), rs.getString("status"));
		return obj;
	}
}
