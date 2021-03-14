package dev.mck.mvnmon.cmd.update;

import io.dropwizard.util.Duration;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.sql.ArtifactDao;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class UpdateMessageHandler implements MessageHandler, Closeable {

  private final Jdbi jdbi;
  private final Queue<Artifact> queue;
  private final int batchSize;
  private final Duration batchInterval;
  private final ScheduledFuture<?> future;

  public UpdateMessageHandler(
      Jdbi jdbi, int batchSize, Duration batchInterval, ScheduledExecutorService executor) {
    this.jdbi = jdbi;
    this.queue = new ConcurrentLinkedQueue<>();
    this.batchSize = batchSize;
    this.batchInterval = batchInterval;
    future =
        executor.scheduleWithFixedDelay(
            this::run,
            batchInterval.getQuantity(),
            batchInterval.getQuantity(),
            batchInterval.getUnit());
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    Artifact artifact = Artifact.parse(msg.getData());
    queue.add(artifact);
    LOG.debug("received artifact={}", artifact);
  }

  private void run() {
    if (queue.isEmpty()) {
      return;
    }
    var dao = jdbi.onDemand(ArtifactDao.class);
    List<Artifact> batch = new ArrayList<>(Math.min(batchSize, queue.size()));
    Artifact artifact;
    for (int i = 0; i < batchSize; i++) {
      artifact = queue.poll();
      if (artifact == null) {
        break;
      }
      batch.add(artifact);
    }
    dao.updateVersions(batch);
    LOG.info("updated batch={}", batch);
  }

  /** Will block until all queued updates are persisted to the database. */
  @Override
  public void close() throws IOException {
    while (!queue.isEmpty()) {
      try {
        Thread.sleep(batchInterval.toMilliseconds());
      } catch (InterruptedException e) {
        LOG.error(
            "interrupted while waiting for queue to drain, {} updates(s) may be lost!",
            queue.size(),
            e);
        break;
      }
    }
    future.cancel(false);
  }
}
