package mck.mvnmon.nats;

import io.dropwizard.lifecycle.Managed;
import io.nats.client.Connection;

public class NatsManager implements Managed {

  private final Connection c;

  public NatsManager(Connection c) {
    this.c = c;
  }

  @Override
  public void start() throws Exception {}

  @Override
  public void stop() throws Exception {
    c.close();
  }
}
