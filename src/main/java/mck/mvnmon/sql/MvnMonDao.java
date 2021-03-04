package mck.mvnmon.sql;

import mck.mvnmon.api.MavenId;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface MvnMonDao {
  @SqlUpdate(
      "insert into mvnmon_mavenids (group, artifact, version, classifier) "
          + "values (:group, :artifact, :version, :classifier)")
  public long insert(@BindFields MavenId id);
}
