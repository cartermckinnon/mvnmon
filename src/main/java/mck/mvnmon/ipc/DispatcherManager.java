package mck.mvnmon.ipc;

import io.dropwizard.lifecycle.Managed;
import io.nats.client.Dispatcher;
import java.time.Duration;

public class DispatcherManager implements Managed {

  private final Dispatcher dispatcher;

  public DispatcherManager(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public void start() throws Exception {}

  @Override
  public void stop() throws Exception {
    dispatcher.drain(Duration.ZERO).get();
  }
}
