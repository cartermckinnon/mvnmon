package mck.mvnmon.db;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import mck.mvnmon.api.MavenId;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
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

  private Connection conn = null;
  private Liquibase liquibase = null;
  private Jdbi jdbi = null;

  @BeforeEach
  public void beforeEach() throws Exception {
    String jdbcUrl = "jdbc:postgresql://localhost:" + postgres.getMappedPort(5432) + "/";
    Properties props = new Properties();
    props.setProperty("user", "user");
    props.setProperty("password", "password");
    conn = DriverManager.getConnection(jdbcUrl, props);
    liquibase =
        new Liquibase(
            "migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(conn));
    liquibase.update((Contexts) null);

    var dataSourceFactory = new DataSourceFactory();
    dataSourceFactory.setUrl(jdbcUrl);
    dataSourceFactory.setDriverClass(Driver.class.getName());
    dataSourceFactory.setUser("user");
    dataSourceFactory.setPassword("password");
    var e = new Environment(MvnMonDaoTest.class.getName());
    jdbi = new JdbiFactory().build(e, dataSourceFactory, "test");
  }

  @AfterEach
  public void afterEach() throws Exception {
    if (liquibase != null) {
      liquibase.dropAll();
    }
    if (conn != null) {
      conn.close();
    }
  }

  @Test
  public void scan() {
    var dao = jdbi.onDemand(MvnMonDao.class);
    var mavenId = new MavenId("group", "artifact", "version", "classifier");
    long id = dao.insert(mavenId);
    var res = dao.scan(100, 0);
    assertThat(res).hasSize(1);
    assertThat(res.get(0)).isEqualTo(mavenId.withId(id));
  }

  @Test
  public void insertAndGet() {
    var dao = jdbi.onDemand(MvnMonDao.class);
    var mavenId = new MavenId("group", "artifact", "version", "classifier");
    long id = dao.insert(mavenId);
    assertThat(dao.get("group", "artifact")).isPresent().get().isEqualTo(mavenId.withId(id));
    // shouldn't be able to insert the same group + artifact twice
    assertThrows(Exception.class, () -> dao.insert(mavenId));
  }

  @Test
  public void update() {
    var dao = jdbi.onDemand(MvnMonDao.class);
    var mavenId = new MavenId("group", "artifact", "version", "classifier");
    long id = dao.insert(mavenId);
    mavenId = mavenId.withId(id).withNewVersion("newVersion");
    dao.update(mavenId);
    assertThat(dao.get("group", "artifact"))
        .isPresent()
        .get()
        .extracting(mid -> mid.getVersion())
        .isEqualTo("newVersion");
  }
}
