package blue.optima.assignment.mapper;

import blue.optima.assignment.model.MappingTable;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MappingTableRowMapper implements RowMapper<MappingTable> {
    @Override
    public MappingTable mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        final MappingTable obj = new MappingTable(rs.getString("user_name"), rs.getString("api"), rs.getInt("rate_limit"));
        return obj;
    }
}