package mck.mvnmon.sql;

import io.dropwizard.util.Duration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.MavenArtifact;
import mck.mvnmon.sink.MavenArtifactSink;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class SqlMavenArtifactSink implements MavenArtifactSink {

  private final Jdbi jdbi;
  private final Queue<MavenArtifact> queue;
  private final int batchSize;
  private final Duration batchInterval;
  private final ScheduledFuture<?> future;

  public SqlMavenArtifactSink(
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
  public void sink(MavenArtifact mavenId) {
    queue.add(mavenId);
  }

  private void run() {
    if (queue.isEmpty()) {
      return;
    }
    var dao = jdbi.onDemand(MavenArtifactDao.class);
    List<MavenArtifact> batch = new ArrayList<>(Math.min(batchSize, queue.size()));
    MavenArtifact mavenId;
    for (int i = 0; i < batchSize; i++) {
      mavenId = queue.poll();
      if (mavenId == null) {
        break;
      }
      batch.add(mavenId);
    }
    dao.update(batch);
  }

  /** Will block until all queued MavenId(s) are persisted to the database. */
  @Override
  public void close() throws IOException {
    while (!queue.isEmpty()) {
      try {
        Thread.sleep(batchInterval.toMilliseconds());
      } catch (InterruptedException e) {
        LOG.error(
            "interrupted while waiting for queue to drain, {} mavenId(s) may be lost!",
            queue.size(),
            e);
        break;
      }
    }
    future.cancel(false);
  }
}
