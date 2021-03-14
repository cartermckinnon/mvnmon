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
}
