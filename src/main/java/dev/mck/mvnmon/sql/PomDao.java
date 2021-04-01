package dev.mck.mvnmon.sql;

import dev.mck.mvnmon.api.maven.Pom;
import dev.mck.mvnmon.sql.mapper.PomRowMapper;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface PomDao {
  @SqlUpdate(
      "INSERT INTO poms (repository_id, path, dependency_hash)"
          + " VALUES (:repositoryId, :path, :dependencyHash)")
  @GetGeneratedKeys
  public long insert(
      @Bind("repositoryId") long repositoryId,
      @Bind("path") String path,
      @Bind("dependencyHash") long dependencyHash);

  @SqlUpdate("DELETE FROM poms WHERE repository_id = :repositoryId AND path = :path")
  public boolean delete(@Bind("repositoryId") long repositoryId, @Bind("path") String path);

  @SqlUpdate(
      "DELETE FROM poms WHERE repository_id = :repositoryId AND path = :path AND dependency_hash"
          + " != :dependencyHash")
  public boolean deleteIfHashDiffers(
      @Bind("repositoryId") long repositoryId,
      @Bind("path") String path,
      @Bind("dependencyHash") long dependencyHash);

  @SqlQuery("SELECT dependency_hash FROM poms WHERE repository_id = :repositoryId AND path = :path")
  public Optional<Long> getDependencyHash(
      @Bind("repositoryId") long repositoryId, @Bind("path") String path);

  @SqlQuery("SELECT * FROM poms WHERE id = :id")
  @RegisterRowMapper(PomRowMapper.class)
  public Optional<Pom> getById(@Bind("id") long id);

  @SqlQuery("SELECT * FROM poms LIMIT :limit")
  @RegisterRowMapper(PomRowMapper.class)
  public List<Pom> get(@Bind("limit") int limit);
}
