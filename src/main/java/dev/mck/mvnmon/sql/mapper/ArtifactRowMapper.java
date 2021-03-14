package dev.mck.mvnmon.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import dev.mck.mvnmon.api.maven.Artifact;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class ArtifactRowMapper implements RowMapper<Artifact> {

  @Override
  public Artifact map(ResultSet rs, StatementContext ctx) throws SQLException {
    // all fields are assumed to be non-nullable
    var versions = new ArrayList<String>();
    ResultSet versionsResultSet = rs.getArray("versions").getResultSet();
    while (versionsResultSet.next()) {
      // idx 1 is array element's idx, idx 2 is array element's value
      versions.add(versionsResultSet.getString(2));
    }
    return new Artifact(rs.getString("group_id"), rs.getString("artifact_id"), versions);
  }
}
