package mck.mvnmon.sql;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import mck.mvnmon.api.maven.Artifact;
import mck.mvnmon.api.maven.ArtifactWithId;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowMapper;

public interface ArtifactDao {

  /**
   * Insert a MavenArtifact if it does not already exist.
   *
   * @param mavenArtifact to be inserted.
   */
  @SqlUpdate(
      "INSERT INTO artifacts (group_id, artifact_id, versions) VALUES (:groupId, :artifactId,"
          + " :versions) ON CONFLICT DO NOTHING")
  // @GetGeneratedKeys
  public void insert(@BindFields Artifact mavenArtifact);

  /**
   * Get a MavenArtifact.
   *
   * @param groupId
   * @param artifactId
   * @return the MavenArtifact, if it exists.
   */
  @SqlQuery("SELECT * FROM artifacts WHERE group_id = :groupId AND artifact_id = :artifactId")
  @UseRowMapper(ArtifactRowMapper.class)
  public Optional<Artifact> get(
      @Bind("groupId") String groupId, @Bind("artifactId") String artifactId);

  /**
   * Scan all MavenArtifactWithId(s) in the table. The 'WithId' variant is used because row ID's are
   * used as the scan cursor.
   *
   * @param limit the number of MavenArtifactWithId(s) to return in the result.
   * @param cursor at the beginning of a scan, pass 0. On subsequent calls, pass the ID of the last
   *     MavenArtifactWithId in the previous result.
   * @return batch of results. When this batch has fewer elements than the limit, the scan is
   *     complete.
   */
  @SqlQuery("SELECT * FROM artifacts WHERE id > :cursor ORDER BY id ASC LIMIT :limit")
  @UseRowMapper(ArtifactWithIdRowMapper.class)
  public List<ArtifactWithId> scan(@Bind("limit") int limit, @Bind("cursor") long cursor);

  public static final String UPDATE_QUERY =
      "UPDATE artifacts SET versions = :versions WHERE group_id = :groupId AND artifact_id ="
          + " :artifactId";

  @SqlUpdate(UPDATE_QUERY)
  public void update(@BindFields Artifact mavenArtifact);

  @SqlBatch(UPDATE_QUERY)
  public void update(@BindFields Collection<Artifact> mavenArtifacts);
}
