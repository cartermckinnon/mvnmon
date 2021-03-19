package dev.mck.mvnmon.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class InstallationDaoTest extends DaoTest {
  @Test
  public void insert() {
    var dao = getJdbi().onDemand(InstallationDao.class);
    dao.insert(1, "login", "token");

    // id clash should result in an exception
    assertThrows(Exception.class, () -> dao.insert(1, "differentLogin", "differentToken"));
  }

  @Test
  public void getToken() {
    var dao = getJdbi().onDemand(InstallationDao.class);
    dao.insert(1, "login", "token");

    assertThat(dao.getToken(1)).isEqualTo("token");
  }

  @Test
  public void delete() {
    var dao = getJdbi().onDemand(InstallationDao.class);

    // empty table
    assertThat(dao.delete(1)).isFalse();

    dao.insert(1, "login", "token");

    assertThat(dao.delete(1)).isTrue();
    assertThat(dao.delete(1)).isFalse();
  }
}
