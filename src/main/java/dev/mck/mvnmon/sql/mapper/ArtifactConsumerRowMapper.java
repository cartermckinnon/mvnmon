package dev.mck.mvnmon.sql.mapper;

import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class ArtifactConsumerRowMapper implements RowMapper<ArtifactConsumer> {

  @Override
  public ArtifactConsumer map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new ArtifactConsumer(
        rs.getLong("pom_id"),
        rs.getString("group_id"),
        rs.getString("artifact_id"),
        rs.getString("current_version"));
  }
}
