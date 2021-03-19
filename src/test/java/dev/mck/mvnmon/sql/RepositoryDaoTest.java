package dev.mck.mvnmon.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RepositoryDaoTest extends DaoTest {
  @Test
  public void insert() {
    var installationDao = getJdbi().onDemand(InstallationDao.class);
    var repositoryDao = getJdbi().onDemand(RepositoryDao.class);

    // non-existant installation
    assertThrows(Exception.class, () -> repositoryDao.insert(1, "name", 1));

    installationDao.insert(1, "login", "token");

    repositoryDao.insert(1, "name", 1);

    // id + name clash
    assertThrows(Exception.class, () -> repositoryDao.insert(1, "name", 1));

    // id clash
    assertThrows(Exception.class, () -> repositoryDao.insert(2, "name", 1));

    // name clash
    assertThrows(Exception.class, () -> repositoryDao.insert(1, "otherName", 1));
  }

  @Test
  public void getToken() {
    var installationDao = getJdbi().onDemand(InstallationDao.class);
    var repositoryDao = getJdbi().onDemand(RepositoryDao.class);

    installationDao.insert(1, "login", "token");

    repositoryDao.insert(1, "name", 1);

    assertThat(repositoryDao.getToken(1)).isEqualTo("token");
  }
}
