package dev.mck.mvnmon.cmd.backend.scheduler;

import com.google.common.base.MoreObjects;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class SchedulerConfiguration {
  @Min(10)
  @Max(100)
  private int batchSize = 100;

  public int getBatchSize() {
    return this.batchSize;
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("batchSize", batchSize).toString();
  }
}
