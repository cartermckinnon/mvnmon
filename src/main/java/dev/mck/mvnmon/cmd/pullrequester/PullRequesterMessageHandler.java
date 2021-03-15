package dev.mck.mvnmon.cmd.pullrequester;

import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.api.maven.ArtifactConsumerWithId;
import dev.mck.mvnmon.sql.ArtifactConsumerDao;
import dev.mck.mvnmon.util.PomFiles;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.kohsuke.github.GitHub;

@Slf4j
public class PullRequesterMessageHandler implements MessageHandler {

  private final int batchSize = 5;

  private final Jdbi jdbi;
  private final GitHub github;
  private final Executor executor;

  public PullRequesterMessageHandler(Jdbi jdbi, GitHub github, Executor executor) {
    this.jdbi = jdbi;
    this.github = github;
    this.executor = executor;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    Artifact artifact = Artifact.parse(msg.getData());
    LOG.debug("received artifact={}", artifact);
    var dao = jdbi.onDemand(ArtifactConsumerDao.class);
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
          executor.execute(new PullRequester(github, consumer, newVersion.get()));
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
