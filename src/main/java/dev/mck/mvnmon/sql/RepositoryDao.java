package dev.mck.mvnmon.sql;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface RepositoryDao {
  @SqlUpdate(
      "INSERT INTO repositories (id, name, installation_id) VALUES (:id, :name, :installationId)")
  public void insert(
      @Bind("id") long id, @Bind("name") String name, @Bind("installationId") long installationId);

  /**
   * @param id repository ID.
   * @return true if the repository and all its related objects were deleted.
   */
  @SqlUpdate("DELETE FROM repositories WHERE id = :id")
  public boolean delete(@Bind("id") long id);

  /**
   * @param id repository ID.
   * @return installation access token.
   */
  @SqlQuery(
      "SELECT token FROM installations WHERE id IN (SELECT installation_id FROM repositories WHERE id = :id)")
  public String getToken(@Bind("id") long id);
}
