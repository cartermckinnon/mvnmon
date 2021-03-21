package dev.mck.mvnmon.sql;

import static org.assertj.core.api.Assertions.*;

import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.api.maven.ArtifactWithId;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class ArtifactDaoTest extends DaoTest {

  @Test
  public void scan() {
    var dao = getJdbi().onDemand(ArtifactDao.class);
    var artifact = new Artifact("group", "artifact", "version");
    dao.insert(artifact);
    var res = dao.scan(100, 0);
    assertThat(res).hasSize(1);
    ArtifactWithId artifactWithId = res.get(0);
    // id's must start at 1 for initial cursor to work correctly
    assertThat(artifactWithId.getId()).isEqualTo(1);
    assertThat(artifactWithId.getGroupId()).isEqualTo("group");
    assertThat(artifactWithId.getArtifactId()).isEqualTo("artifact");
    assertThat(artifactWithId.getVersions()).containsExactly("version");
  }

  @Test
  public void insertAndGet() {
    var dao = getJdbi().onDemand(ArtifactDao.class);
    var artifact = new Artifact("group", "artifact", "version");
    dao.insert(artifact);
    assertThat(dao.get("group", "artifact")).isPresent().get().isEqualTo(artifact);
    // shouldn't be able to insert the same group + artifact twice
    artifact = new Artifact("group", "artifact", "otherVersion");
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
    var dao = getJdbi().onDemand(ArtifactDao.class);
    var artifact = new Artifact("group", "artifact", "version");
    dao.insert(artifact);
    artifact = artifact.withVersions("newVersion", "newVersion2");
    dao.updateVersions(artifact);
    assertThat(dao.get("group", "artifact"))
        .isPresent()
        .get()
        .extracting(mid -> mid.getVersions())
        .asList()
        .containsExactly("newVersion", "newVersion2");
  }

  @Test
  public void deleteIfNoConsumers() {
    var dao = getJdbi().onDemand(ArtifactDao.class);

    Artifact artifactA = new Artifact("groupIdA", "artifactIdA", "version");
    Artifact artifactB = new Artifact("groupIdB", "artifactIdB", "version");

    dao.insert(artifactA);
    dao.insert(artifactB);

    // both should be deleted when consumers table is empty
    assertThat(dao.deleteArtifactsWithoutConsumers()).isEqualTo(2);
    assertThat(dao.get("groupIdA", "artifactIdA")).isEmpty();
    assertThat(dao.get("groupIdB", "artifactIdB")).isEmpty();

    dao.insert(artifactA);
    dao.insert(artifactB);

    getJdbi().onDemand(InstallationDao.class).insert(0, "login", "token");
    getJdbi().onDemand(RepositoryDao.class).insert(0, "name", 0);
    long pomId = getJdbi().onDemand(PomDao.class).insert(0, "path", 0);
    getJdbi().onDemand(ArtifactConsumerDao.class).upsert(pomId, "groupIdA", "artifactIdA", "version");

    // B should be deleted because it had no consumers
    assertThat(dao.deleteArtifactsWithoutConsumers()).isEqualTo(1);
    assertThat(dao.get("groupIdA", "artifactIdA")).isPresent();
    assertThat(dao.get("groupIdB", "artifactIdB")).isEmpty();
  }
}
