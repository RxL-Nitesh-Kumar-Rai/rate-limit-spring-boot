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


	public void updateStatus(final String status, final ThrottlingConfiguration throttlingConfiguration) {

		//fill in the code.
	}

	public int update(final ThrottlingConfiguration throttlingConfiguration) {

		//fill in the code.
		return 0;

	}

	//This will always create a new object in the DB.If it is not present.Otherwise..if the object is present and it is different then the existing one
	//then mark it inactive and make a new entry.
	public int create(final ThrottlingConfiguration throttlingConfiguration) {

		//Fill in the code
		return 0;
	}

}
