package mck.mvnmon.sql;

import static org.assertj.core.api.Assertions.*;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import mck.mvnmon.api.MavenArtifact;
import mck.mvnmon.api.MavenArtifactWithId;
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
public class MavenArtifactDaoTest {
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
    var e = new Environment(MavenArtifactDaoTest.class.getName());
    jdbi = new PostgresJdbiFactory().build(e, dataSourceFactory, "test");
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
    var dao = jdbi.onDemand(MavenArtifactDao.class);
    var artifact = new MavenArtifact("group", "artifact", "version");
    dao.insert(artifact);
    var res = dao.scan(100, 0);
    assertThat(res).hasSize(1);
    MavenArtifactWithId artifactWithId = res.get(0);
    // id's must start at 1 for initial cursor to work correctly
    assertThat(artifactWithId.getId()).isEqualTo(1);
    assertThat(artifactWithId.getGroupId()).isEqualTo("group");
    assertThat(artifactWithId.getArtifactId()).isEqualTo("artifact");
    assertThat(artifactWithId.getVersions()).containsExactly("version");
  }

  @Test
  public void insertAndGet() {
    var dao = jdbi.onDemand(MavenArtifactDao.class);
    var artifact = new MavenArtifact("group", "artifact", "version");
    dao.insert(artifact);
    assertThat(dao.get("group", "artifact")).isPresent().get().isEqualTo(artifact);
    // shouldn't be able to insert the same group + artifact twice
    artifact = new MavenArtifact("group", "artifact", "otherVersion");
    dao.insert(artifact);
    // version should not have changed since first insert
    assertThat(dao.get("group", "artifact"))
        .isPresent()
        .get()
        .extracting("versions")
        .asList()
        .containsExactly("version");
  }

  @Test
  public void update() {
    var dao = jdbi.onDemand(MavenArtifactDao.class);
    var artifact = new MavenArtifact("group", "artifact", "version");
    dao.insert(artifact);
    artifact = artifact.withVersions("newVersion", "newVersion2");
    dao.update(artifact);
    assertThat(dao.get("group", "artifact"))
        .isPresent()
        .get()
        .extracting(mid -> mid.getVersions())
        .asList()
        .containsExactly("newVersion", "newVersion2");
  }
}
