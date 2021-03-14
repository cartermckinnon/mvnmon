package dev.mck.mvnmon.nats;

import io.dropwizard.lifecycle.Managed;
import io.nats.client.Connection;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NatsManager implements Managed {

  private final Connection c;

  public NatsManager(Connection c) {
    this.c = c;
  }

  @Override
  public void start() throws Exception {}

  @Override
  public void stop() {
    LOG.warn("draining...");
    try {
      c.drain(Duration.ZERO);
    } catch (Exception e) {
      LOG.error("failed to drain!", e);
    }
    LOG.warn("closing...");
    try {
      c.close();
    } catch (Exception e) {
      LOG.error("failed to close!", e);
    }
  }
}
