package dev.mck.mvnmon.cmd.backend.webhooks;

import static java.util.stream.Collectors.toList;

import de.pdark.decentxml.Document;
import dev.mck.mvnmon.api.github.PushEvent;
import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.nats.TypedHandler;
import dev.mck.mvnmon.sql.ArtifactConsumerDao;
import dev.mck.mvnmon.sql.ArtifactDao;
import dev.mck.mvnmon.sql.PomDao;
import dev.mck.mvnmon.sql.RepositoryDao;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.XmlFiles;
import java.io.IOException;
import org.jdbi.v3.core.Jdbi;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives GitHub webhook "push" events for POM changes, creating artifact consumers accordingly.
 */
public class PushEventHandler extends TypedHandler<PushEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(PushEventHandler.class);

  private final Jdbi jdbi;

  public PushEventHandler(Jdbi jdbi) {
    super(PushEvent.class);
    this.jdbi = jdbi;
  }

  @Override
  protected void handlePayload(PushEvent event) throws Exception {
    if (!event.isToDefaultBranch()) {
      return;
    }
    var poms = event.getPomPaths();
    if (poms.isEmpty()) {
      return;
    }
    var repositoryDao = jdbi.onDemand(RepositoryDao.class);
    String token = repositoryDao.getToken(event.repository().id());
    GitHub github = GitHub.connectUsingOAuth(token);
    GHRepository ghRepo;
    try {
      ghRepo = github.getRepository(event.repository().name());
    } catch (IOException e) {
      throw new IllegalStateException("failed to get repository for push=" + event, e);
    }
    for (String pom : poms) {
      Document doc;
      try {
        doc = XmlFiles.parse(ghRepo.getFileContent(pom, event.ref()).read());
      } catch (IOException e) {
        throw new IllegalArgumentException("failed to download and parse pom=" + pom, e);
      }
      var dependencies = PomFiles.getDependencies(doc);

      // if the hash of the dependencies block has changed, delete the pom
      // which will cascade and delete the relevent consumers
      var dependencyHash = PomFiles.hashDependencies(dependencies);
      var pomDao = jdbi.onDemand(PomDao.class);
      var currentDependencyHash = pomDao.getDependencyHash(event.repository().id(), pom);
      if (currentDependencyHash.isPresent()) {
        if (dependencyHash != currentDependencyHash.get()) {
          pomDao.delete(event.repository().id(), pom);
        } else {
          // no dependency changes, we're done here
          LOG.info("unchanged pom={}", pom);
          continue;
        }
      }
      // insert the current dependency hash
      long pomId = pomDao.insert(event.repository().id(), pom, dependencyHash);

      // ensure that all depended-on artifacts exist in the artifacts table
      var artifacts =
          dependencies.stream()
              .map(d -> new Artifact(d.getGroupId(), d.getArtifactId(), d.getVersion()))
              .collect(toList());
      var artifactDao = jdbi.onDemand(ArtifactDao.class);
      artifactDao.insert(artifacts);

      // insert all the consumed artifacts in this POM
      var consumers =
          dependencies.stream()
              .map(
                  d ->
                      new ArtifactConsumer(
                          pomId, d.getGroupId(), d.getArtifactId(), d.getVersion()))
              .collect(toList());
      var consumerDao = jdbi.onDemand(ArtifactConsumerDao.class);
      consumerDao.upsert(consumers);

      LOG.info(
          "created consumers={} pom={} repository={}",
          consumers.size(),
          pom,
          event.repository().name());
    }
  }
}
