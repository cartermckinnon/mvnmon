package dev.mck.mvnmon.sql.mapper;

import dev.mck.mvnmon.api.maven.Pom;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class PomRowMapper implements RowMapper<Pom> {

  @Override
  public Pom map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Pom(
        rs.getLong("repository_id"), rs.getString("path"), rs.getLong("dependency_hash"));
  }
}
