package dev.mck.mvnmon.nats;

import io.dropwizard.lifecycle.Managed;
import io.nats.client.Dispatcher;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DispatcherManager implements Managed {

  private final Dispatcher dispatcher;

  public DispatcherManager(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public void start() throws Exception {}

  @Override
  public void stop() throws Exception {
    LOG.warn("draining...");
    dispatcher.drain(Duration.ZERO).get();
  }
}
