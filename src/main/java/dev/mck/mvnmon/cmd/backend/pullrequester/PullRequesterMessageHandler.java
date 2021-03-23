package dev.mck.mvnmon.cmd.backend.pullrequester;

import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.api.maven.ArtifactConsumerWithId;
import dev.mck.mvnmon.nats.TypedHandler;
import dev.mck.mvnmon.sql.ArtifactConsumerDao;
import dev.mck.mvnmon.sql.PomDao;
import dev.mck.mvnmon.util.Pair;
import dev.mck.mvnmon.util.PomFiles;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullRequesterMessageHandler extends TypedHandler<Artifact> {

  private static final Logger LOG = LoggerFactory.getLogger(PullRequesterMessageHandler.class);

  private final int batchSize = 5;
  private final Jdbi jdbi;
  private final Executor executor;

  public PullRequesterMessageHandler(Jdbi jdbi, Executor executor) {
    super(Artifact.class);
    this.jdbi = jdbi;
    this.executor = executor;
  }

  @Override
  protected void handlePayload(Artifact artifact) {
    var dao = jdbi.onDemand(ArtifactConsumerDao.class);
    var pomDao = jdbi.onDemand(PomDao.class);
    long cursor = 0;
    List<ArtifactConsumerWithId> consumers;
    int n = 0;
    while (true) {
      consumers = dao.scan(artifact.getGroupId(), artifact.getArtifactId(), batchSize, cursor);
      if (consumers.isEmpty()) {
        break;
      }
      for (ArtifactConsumer consumer : consumers) {
        Optional<Pair<String, List<String>>> newVersion =
            PomFiles.getNewVersion(consumer.getCurrentVersion(), artifact.getVersions());
        if (newVersion.isPresent()) {
          var pom = pomDao.getById(consumer.getPomId());
          if (pom.isEmpty()) {
            // something has gone horribly wrong
            throw new IllegalStateException("pom does not exist for consumer=" + consumer);
          }
          executor.execute(
              new PullRequester(
                  jdbi,
                  pom.get(),
                  consumer,
                  newVersion.get().getLeft(),
                  newVersion.get().getRight()));
          n++;
        }
      }
      if (consumers.size() < batchSize) {
        // this batch was the last one
        break;
      }
      cursor = consumers.get(consumers.size() - 1).getId();
    }
    LOG.info("created {} pull request(s) for artifact={}", n, artifact);
  }
}
