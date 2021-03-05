package mck.mvnmon.db;

import java.util.List;
import mck.mvnmon.api.MavenId;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowMapper;

public interface MvnMonDao {
  @SqlUpdate(
      "insert into mvnmon_ids (grp, art, ver, cls) "
          + "values (:group, :artifact, :version, :classifier)")
  @GetGeneratedKeys
  public long insert(@BindFields MavenId id);

  @SqlQuery("SELECT * FROM mvnmon_ids WHERE id > :cursor LIMIT :limit")
  @UseRowMapper(MavenIdRowMapper.class)
  public List<MavenId> get(@Bind("limit") int limit, @Bind("cursor") long cursor);
}
