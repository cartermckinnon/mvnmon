package dev.mck.mvnmon.sql;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface InstallationDao {
  @SqlUpdate("INSERT INTO installations (id, login, token) VALUES (:id, :login, :token)")
  public void insert(@Bind("id") long id, @Bind("login") String login, @Bind("token") String token);

  @SqlQuery("SELECT token FROM installations WHERE id = :id")
  public String getToken(@Bind("id") long id);

  @SqlUpdate("DELETE FROM installations WHERE id = :id")
  public boolean delete(@Bind("id") long id);
}
