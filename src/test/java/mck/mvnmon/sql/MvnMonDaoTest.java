package mck.mvnmon.sql;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import liquibase.pro.packaged.e;
import mck.mvnmon.api.MavenId;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.Driver;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class MvnMonDaoTest {
  @Container
  public GenericContainer postgres =
      new GenericContainer(DockerImageName.parse("postgres:latest"))
          .withEnv("POSTGRES_USER", "user")
          .withEnv("POSTGRES_PASSWORD", "password")
          .withExposedPorts(5432);

  private Jdbi jdbi = null;

  @BeforeEach
  public void beforeAll() {
    var configuration = new DataSourceFactory();
    configuration.setDriverClass(Driver.class.getName());
    configuration.setUrl("jdbc:postgresql://localhost:" + postgres.getMappedPort(5432) + "/");
    configuration.setUser("user");
    configuration.setPassword("password");
    Environment e = new Environment(MvnMonDaoTest.class.getName());
    jdbi = new JdbiFactory().build(e, configuration, MvnMonDaoTest.class.getName());
  }

  @Test
  public void insert() {
    var dao = jdbi.onDemand(MvnMonDao.class);
    var id = new MavenId("group", "artifact", "version", "classifier");
    dao.insert(id);
  }
}
