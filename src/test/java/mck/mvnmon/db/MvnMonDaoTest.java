package mck.mvnmon.db;

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
  public void get() {
    var dao = jdbi.onDemand(MvnMonDao.class);
    var res = dao.get(100, 0);
  }

  @Test
  public void insert() {
    var dao = jdbi.onDemand(MvnMonDao.class);
    var id = new MavenId("group", "artifact", "version", "classifier");
    dao.insert(id);
  }
}
