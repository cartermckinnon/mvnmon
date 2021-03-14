package dev.mck.mvnmon.sql;

import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.api.maven.ArtifactConsumerWithId;
import dev.mck.mvnmon.sql.mapper.ArtifactConsumerRowMapper;
import dev.mck.mvnmon.sql.mapper.ArtifactConsumerWithIdRowMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowMapper;

public interface ArtifactConsumerDao {

  public static final String UPSERT_QUERY =
      "INSERT INTO consumers (repository, pom, group_id, artifact_id, current_version)"
          + " VALUES (:repository, :pom, :groupId, :artifactId, :currentVersion)"
          + " ON CONFLICT ON CONSTRAINT repository_pom_group_id_artifact_id"
          + " DO UPDATE SET current_version = :currentVersion";

  /**
   * Insert an artifact consumer, updating it if it already exists.
   *
   * @param consumer to be upserted.
   */
  @SqlUpdate(UPSERT_QUERY)
  public void upsert(@BindFields ArtifactConsumer consumer);

  /**
   * Insert a batch of artifact consumers, updating them if they already exist.
   *
   * @param consumers to be upserted.
   */
  @SqlBatch(UPSERT_QUERY)
  public void upsert(@BindFields Collection<ArtifactConsumer> consumers);

  /**
   * Get an artifact consumer.
   *
   * @param repository
   * @param pom
   * @param groupId
   * @param artifactId
   * @return the MavenArtifact, if it exists.
   */
  @SqlQuery(
      "SELECT * FROM consumers"
          + " WHERE repository = :repository"
          + " AND pom = :pom"
          + " AND group_id = :groupId"
          + " AND artifact_id = :artifactId")
  @UseRowMapper(ArtifactConsumerRowMapper.class)
  public Optional<ArtifactConsumer> get(
      @Bind("repository") String repository,
      @Bind("pom") String pom,
      @Bind("groupId") String groupId,
      @Bind("artifactId") String artifactId);

  /**
   * Scan all consumers in the table for an artifact.
   *
   * <p>The 'WithId' variant is returned because row ID's are used as the scan cursor.
   *
   * @param groupId
   * @param artifactId
   * @param limit the number of rows to return in the result.
   * @param cursor at the beginning of a scan, pass 0. On subsequent calls, pass the ID of the last
   *     row in the previous result.
   * @return batch of rows. When this batch has fewer elements than the limit, the scan is complete.
   */
  @SqlQuery(
      "SELECT * FROM consumers"
          + " WHERE id > :cursor AND group_id = :groupId AND artifact_id = :artifactId"
          + " ORDER BY id ASC LIMIT :limit")
  @UseRowMapper(ArtifactConsumerWithIdRowMapper.class)
  public List<ArtifactConsumerWithId> scan(
      @Bind("groupId") String groupId,
      @Bind("artifactId") String artifactId,
      @Bind("limit") int limit,
      @Bind("cursor") long cursor);

  public static final String UPDATE_CURRENT_VERSION_QUERY =
      "UPDATE consumers SET current_version = :currentVersion"
          + " WHERE repository = :repository"
          + " AND pom = :pom"
          + " AND group_id = :groupId "
          + " AND artifact_id = :artifactId";

  /**
   * Update the current version for an artifact consumer.
   *
   * @param consumer
   */
  @SqlUpdate(UPDATE_CURRENT_VERSION_QUERY)
  public void updateCurrentVersion(@BindFields ArtifactConsumer consumer);

  /**
   * Update the current version for a batch of artifact consumers.
   *
   * @param consumers
   */
  @SqlBatch(UPDATE_CURRENT_VERSION_QUERY)
  public void updateCurrentVersion(@BindFields Collection<ArtifactConsumer> consumers);

  /** Delete all the artifact consumers for a (repository, pom) pair. */
  @SqlUpdate("DELETE FROM consumers WHERE repository = :repository AND pom = :pom")
  public void delete(@Bind("repository") String repository, @Bind("pom") String pom);
}
