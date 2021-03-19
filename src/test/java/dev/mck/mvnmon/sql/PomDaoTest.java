package dev.mck.mvnmon.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import dev.mck.mvnmon.api.maven.Pom;
import org.junit.jupiter.api.Test;

public class PomDaoTest extends DaoTest {
  @Test
  public void insert() {
    var installationDao = getJdbi().onDemand(InstallationDao.class);
    installationDao.insert(1, "login", "token");

    var repositoryDao = getJdbi().onDemand(RepositoryDao.class);
    repositoryDao.insert(1, "repository", 1);

    var dao = getJdbi().onDemand(PomDao.class);

    long id = dao.insert(1, "path", 123);
    assertThrows(Exception.class, () -> dao.insert(1, "path", 456));

    var expected = new Pom(1, "path", 123);

    var pom = dao.getById(id);

    assertThat(pom).isPresent().get().isEqualTo(expected);
  }
}
