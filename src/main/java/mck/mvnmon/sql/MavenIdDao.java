package mck.mvnmon.sql;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import mck.mvnmon.api.MavenId;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowMapper;

public interface MavenIdDao {

  /**
   * Insert a MavenId.
   *
   * @param mavenId to be inserted. The 'id' field will be ignored.
   * @return generated ID.
   */
  @SqlUpdate(
      "INSERT INTO mvnmon_ids (grp, art, ver, cls) VALUES (:group, :artifact, :version,"
          + " :classifier)")
  @GetGeneratedKeys
  public long insert(@BindFields MavenId mavenId);

  /**
   * Get a MavenId.
   *
   * @param group group identifier.
   * @param artifact artifact identifier.
   * @return the MavenId, if it exists.
   */
  @SqlQuery("SELECT * FROM mvnmon_ids WHERE grp = :group AND art = :artifact")
  @UseRowMapper(MavenIdRowMapper.class)
  public Optional<MavenId> get(@Bind("group") String group, @Bind("artifact") String artifact);

  /**
   * Scan all MavenId(s) in the table.
   *
   * @param limit the number of MavenId(s) to return in the result.
   * @param cursor at the beginning of a scan, 0. On subsequent calls, the ID of the last MavenId in
   *     the previous result.
   * @return next batch of results. When this batch has fewer elements than the limit, the scan is
   *     complete.
   */
  @SqlQuery("SELECT * FROM mvnmon_ids WHERE id > :cursor ORDER BY id ASC LIMIT :limit")
  @UseRowMapper(MavenIdRowMapper.class)
  public List<MavenId> scan(@Bind("limit") int limit, @Bind("cursor") long cursor);

  public static final String UPDATE_QUERY =
      "UPDATE mvnmon_ids SET grp = :group, art = :artifact, ver = :version, cls = :classifier"
          + " WHERE id = :id";

  @SqlUpdate(UPDATE_QUERY)
  public void update(@BindFields MavenId mavenId);

  @SqlBatch(UPDATE_QUERY)
  public void update(@BindFields Collection<MavenId> mavenIds);
}
