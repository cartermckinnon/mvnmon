package dev.mck.mvnmon.cmd.backend.updater;

import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.nats.TypedHandler;
import dev.mck.mvnmon.sql.ArtifactDao;
import io.dropwizard.util.Duration;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdaterMessageHandler extends TypedHandler<Artifact> implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(UpdaterMessageHandler.class);

  private final Jdbi jdbi;
  private final Queue<Artifact> queue;
  private final int batchSize;
  private final Duration batchInterval;
  private final ScheduledFuture<?> future;

  public UpdaterMessageHandler(
      Jdbi jdbi, int batchSize, Duration batchInterval, ScheduledExecutorService executor) {
    super(Artifact.class);
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
  protected void handlePayload(Artifact artifact) {
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
