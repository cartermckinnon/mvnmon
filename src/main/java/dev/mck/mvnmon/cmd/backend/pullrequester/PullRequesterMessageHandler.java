package dev.mck.mvnmon.cmd.backend.pullrequester;

import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.api.maven.ArtifactConsumerWithId;
import dev.mck.mvnmon.sql.ArtifactConsumerDao;
import dev.mck.mvnmon.sql.PomDao;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.Serialization;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullRequesterMessageHandler implements MessageHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PullRequesterMessageHandler.class);

  private final int batchSize = 5;
  private final Jdbi jdbi;
  private final Executor executor;

  public PullRequesterMessageHandler(Jdbi jdbi, Executor executor) {
    this.jdbi = jdbi;
    this.executor = executor;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    Artifact artifact = Serialization.deserialize(msg.getData(), Artifact.class);
    LOG.debug("received artifact={}", artifact);
    var dao = jdbi.onDemand(ArtifactConsumerDao.class);
    var pomDao = jdbi.onDemand(PomDao.class);
    long cursor = 0;
    List<ArtifactConsumerWithId> consumers;
    while (true) {
      consumers = dao.scan(artifact.getGroupId(), artifact.getArtifactId(), batchSize, cursor);
      if (consumers.isEmpty()) {
        break;
      }
      for (ArtifactConsumer consumer : consumers) {
        Optional<String> newVersion =
            PomFiles.getNewVersion(consumer.getCurrentVersion(), artifact.getVersions());
        if (newVersion.isPresent()) {
          var pom = pomDao.getById(consumer.getPomId());
          if (pom.isEmpty()) {
            // something has gone horribly wrong
            throw new IllegalStateException("pom does not exist for consumer=" + consumer);
          }
          executor.execute(new PullRequester(jdbi, pom.get(), consumer, newVersion.get()));
        }
      }
      if (consumers.size() < batchSize) {
        // this batch was the last one
        break;
      }
      cursor = consumers.get(consumers.size() - 1).getId();
    }
  }
}
