package dev.mck.mvnmon.sql.mapper;

import dev.mck.mvnmon.api.maven.ArtifactConsumerWithId;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class ArtifactConsumerWithIdRowMapper implements RowMapper<ArtifactConsumerWithId> {

  @Override
  public ArtifactConsumerWithId map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new ArtifactConsumerWithId(
        rs.getLong("id"),
        rs.getLong("pom_id"),
        rs.getString("group_id"),
        rs.getString("artifact_id"),
        rs.getString("current_version"));
  }
}
