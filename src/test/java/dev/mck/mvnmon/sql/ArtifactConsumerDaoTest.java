package dev.mck.mvnmon.sql;

import dev.mck.mvnmon.sql.ArtifactConsumerDao;
import dev.mck.mvnmon.sql.ArtifactDao;
import static org.assertj.core.api.Assertions.assertThat;

import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import org.junit.jupiter.api.Test;

public class ArtifactConsumerDaoTest extends DaoTest {

  @Test
  public void scan() {
    var artifactDao = getJdbi().onDemand(ArtifactDao.class);
    var artifact = new Artifact("group", "artifact", "version");
    artifactDao.insert(artifact);

    var dao = getJdbi().onDemand(ArtifactConsumerDao.class);
    var consumer = new ArtifactConsumer("repository", "pom.xml", "group", "artifact", "version");
    dao.upsert(consumer);

    var res = dao.scan("group", "artifact", 100, 0);
    assertThat(res).hasSize(1);

    // id's must start at 1 for initial cursor to work correctly
    var consumerWithId = res.get(0);
    assertThat(consumerWithId.getId()).isEqualTo(1);
    assertThat(consumerWithId.getRepository()).isEqualTo("repository");
    assertThat(consumerWithId.getPom()).isEqualTo("pom.xml");
    assertThat(consumerWithId.getGroupId()).isEqualTo("group");
    assertThat(consumerWithId.getArtifactId()).isEqualTo("artifact");
    assertThat(consumerWithId.getCurrentVersion()).isEqualTo("version");

    assertThat(dao.scan("group", "artifact", 100, 1)).isEmpty();
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
