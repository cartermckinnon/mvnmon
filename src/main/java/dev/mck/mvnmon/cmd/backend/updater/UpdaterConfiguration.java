package dev.mck.mvnmon.cmd.backend.updater;

import com.google.common.base.MoreObjects;
import dev.mck.mvnmon.conf.JdbiAndNatsConfiguration;
import io.dropwizard.util.Duration;

public class UpdaterConfiguration extends JdbiAndNatsConfiguration {
  private int batchSize = 100;
  private Duration interval = Duration.seconds(10);

  public int getBatchSize() {
    return this.batchSize;
  }

  public Duration getInterval() {
    return this.interval;
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }

  public void setInterval(final Duration interval) {
    this.interval = interval;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("batchSize", batchSize)
        .add("interval", interval)
        .toString();
  }
}
