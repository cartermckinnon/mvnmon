package mck.mvnmon.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import mck.mvnmon.api.maven.ArtifactConsumer;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class ArtifactConsumerRowMapper implements RowMapper<ArtifactConsumer> {

  @Override
  public ArtifactConsumer map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new ArtifactConsumer(
        rs.getString("repository"),
        rs.getString("pom"),
        rs.getString("group_id"),
        rs.getString("artifact_id"),
        rs.getString("current_version"));
  }
}
