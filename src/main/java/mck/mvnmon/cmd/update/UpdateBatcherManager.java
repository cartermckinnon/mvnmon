package mck.mvnmon.cmd.update;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateBatcherManager implements Managed {

  private final UpdateBatcher updateBatcher;
  private final Duration batchInterval;

  public UpdateBatcherManager(UpdateBatcher updateBatcher, Duration batchInterval) {
    this.updateBatcher = updateBatcher;
    this.batchInterval = batchInterval;
  }

  @Override
  public void start() throws Exception {}

  @Override
  public void stop() throws Exception {
    // block until the batcher's queue is drained
    int queueSize = updateBatcher.queueSize();
    while (queueSize > 0) {
      LOG.warn("draining {} queued maven id(s)...", queueSize);
      Thread.sleep(batchInterval.toMilliseconds());
      queueSize = updateBatcher.queueSize();
    }
  }
}
