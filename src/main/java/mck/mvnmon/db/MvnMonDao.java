package mck.mvnmon.db;

import java.util.Collection;
import java.util.List;
import mck.mvnmon.api.MavenId;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowMapper;

public interface MvnMonDao {
  @SqlUpdate(
      "INSERT INTO mvnmon_ids (grp, art, ver, cls) VALUES (:group, :artifact, :version,"
          + " :classifier)")
  @GetGeneratedKeys
  public long insert(@BindFields MavenId id);

  @SqlQuery("SELECT * FROM mvnmon_ids WHERE id > :cursor LIMIT :limit")
  @UseRowMapper(MavenIdRowMapper.class)
  public List<MavenId> get(@Bind("limit") int limit, @Bind("cursor") long cursor);

  public static final String UPDATE_QUERY =
      "UPDATE mvnmon_ids SET grp = :group, art = :artifact, ver = :version, cls = :classifier"
          + " WHERE id = :id";

  @SqlUpdate(UPDATE_QUERY)
  public void update(@BindFields MavenId mavenId);

  @SqlBatch(UPDATE_QUERY)
  public void update(@BindFields Collection<MavenId> mavenIds);
}
