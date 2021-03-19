package dev.mck.mvnmon.sql;

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

    var installationDao = getJdbi().onDemand(InstallationDao.class);
    installationDao.insert(1, "login", "token");

    var repositoryDao = getJdbi().onDemand(RepositoryDao.class);
    repositoryDao.insert(1, "repository", 1);

    var pomDao = getJdbi().onDemand(PomDao.class);
    var pomId = pomDao.insert(1, "pom.xml", 1);

    var dao = getJdbi().onDemand(ArtifactConsumerDao.class);
    var consumer = new ArtifactConsumer(pomId, "group", "artifact", "version");
    dao.upsert(consumer);

    var res = dao.scan("group", "artifact", 100, 0);
    assertThat(res).hasSize(1);

    // id's must start at 1 for initial cursor to work correctly
    var consumerWithId = res.get(0);
    assertThat(consumerWithId.getId()).isEqualTo(1);
    assertThat(consumerWithId.getPomId()).isEqualTo(1);
    assertThat(consumerWithId.getGroupId()).isEqualTo("group");
    assertThat(consumerWithId.getArtifactId()).isEqualTo("artifact");
    assertThat(consumerWithId.getCurrentVersion()).isEqualTo("version");

    assertThat(dao.scan("group", "artifact", 100, 1)).isEmpty();
  }
}
