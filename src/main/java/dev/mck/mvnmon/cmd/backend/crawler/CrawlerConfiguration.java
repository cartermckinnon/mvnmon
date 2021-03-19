package dev.mck.mvnmon.cmd.backend.crawler;

import com.google.common.base.MoreObjects;
import javax.validation.constraints.Min;

public class CrawlerConfiguration {
  @Min(1)
  private int maxConcurrentRequests = 32;

  public int getMaxConcurrentRequests() {
    return this.maxConcurrentRequests;
  }

  public void setMaxConcurrentRequests(final int maxConcurrentRequests) {
    this.maxConcurrentRequests = maxConcurrentRequests;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("maxConcurrentRequests", maxConcurrentRequests)
        .toString();
  }
}
