package dev.mck.mvnmon.cmd.backend.webhooks;

import de.pdark.decentxml.Document;
import dev.mck.mvnmon.api.github.Installation;
import dev.mck.mvnmon.api.github.Repository;
import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.sql.ArtifactConsumerDao;
import dev.mck.mvnmon.sql.ArtifactDao;
import dev.mck.mvnmon.sql.PomDao;
import dev.mck.mvnmon.sql.RepositoryDao;
import dev.mck.mvnmon.util.Pair;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.XmlFiles;
import java.util.Collection;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RepositoryInitHandler<T> extends TypedHandler<T> {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryInitHandler.class);

  private final Jdbi jdbi;

  public RepositoryInitHandler(Class<T> eventClass, Jdbi jdbi) {
    super(eventClass);
    this.jdbi = jdbi;
  }

  protected Jdbi getJdbi() {
    return jdbi;
  }

  @Override
  protected void handlePayload(T event) {
    try {
      var installation = getInstallationAndToken(event);
      var repositories = getRepositories(event);

      var artifactDao = jdbi.onDemand(ArtifactDao.class);
      var consumerDao = jdbi.onDemand(ArtifactConsumerDao.class);
      var repositoryDao = jdbi.onDemand(RepositoryDao.class);
      var pomDao = jdbi.onDemand(PomDao.class);
      var github = GitHub.connectUsingOAuth(installation.getRight());

      for (Repository repository : repositories) {
        repositoryDao.insert(
            repository.getId(), repository.getName(), installation.getLeft().getId());

        List<GHContent> poms =
            github.searchContent().repo(repository.getName()).filename("pom.xml").list().toList();

        for (GHContent pom : poms) {
          Document doc = XmlFiles.parse(pom.read());
          var dependencies = PomFiles.getDependencies(doc);

          var artifacts =
              dependencies.stream()
                  .map(d -> new Artifact(d.getGroupId(), d.getArtifactId(), d.getVersion()))
                  .toList();
          artifactDao.insert(artifacts);

          var dependencyHash = PomFiles.hashDependencies(dependencies);
          long pomId = pomDao.insert(repository.getId(), pom.getPath(), dependencyHash);

          var consumers =
              dependencies.stream()
                  .map(
                      d ->
                          new ArtifactConsumer(
                              pomId, d.getGroupId(), d.getArtifactId(), d.getVersion()))
                  .toList();
          consumerDao.upsert(consumers);

          LOG.info(
              "inserted consumers={} pom={} repository={}",
              consumers.size(),
              pom.getPath(),
              repository.getName());
        }
      }
    } catch (Exception e) {
      LOG.error("failed to handle event={}", event, e);
    }
  }

  protected abstract Pair<Installation, String> getInstallationAndToken(T event) throws Exception;

  protected abstract Collection<Repository> getRepositories(T event) throws Exception;
}
