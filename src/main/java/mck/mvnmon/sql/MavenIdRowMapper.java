package mck.mvnmon.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import mck.mvnmon.api.MavenId;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class MavenIdRowMapper implements RowMapper<MavenId> {

  @Override
  public MavenId map(ResultSet rs, StatementContext ctx) throws SQLException {
    // none of these fields are (assumed to be) nullable in the table
    return new MavenId(
        rs.getLong("id"), rs.getString("grp"), rs.getString("art"), rs.getString("ver"));
  }
}
