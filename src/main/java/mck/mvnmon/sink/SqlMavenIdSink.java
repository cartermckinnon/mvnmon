package mck.mvnmon.sink;

import io.dropwizard.util.Duration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.sql.MavenIdDao;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class SqlMavenIdSink implements MavenIdSink {

  private final Jdbi jdbi;
  private final Queue<MavenId> queue;
  private final int batchSize;
  private final Duration batchInterval;
  private final ScheduledFuture future;

  public SqlMavenIdSink(
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
  public void sink(MavenId mavenId) {
    queue.add(mavenId);
  }

  private void run() {
    if (queue.isEmpty()) {
      return;
    }
    var dao = jdbi.onDemand(MavenIdDao.class);
    List<MavenId> batch = new ArrayList<>(Math.min(batchSize, queue.size()));
    MavenId mavenId;
    for (int i = 0; i < batchSize; i++) {
      mavenId = queue.poll();
      if (mavenId == null) {
        break;
      }
      batch.add(mavenId);
    }
    dao.update(batch);
  }

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
