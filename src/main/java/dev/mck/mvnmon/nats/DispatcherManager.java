package dev.mck.mvnmon.nats;

import io.dropwizard.lifecycle.Managed;
import io.nats.client.Dispatcher;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherManager implements Managed {

  private static final Logger LOG = LoggerFactory.getLogger(DispatcherManager.class);

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
