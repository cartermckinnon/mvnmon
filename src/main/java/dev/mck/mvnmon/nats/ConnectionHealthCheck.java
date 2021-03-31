package dev.mck.mvnmon.nats;

import com.codahale.metrics.health.HealthCheck;
import io.nats.client.Connection;
import io.nats.client.Connection.Status;

public class ConnectionHealthCheck extends HealthCheck {

  private final Connection nats;

  public ConnectionHealthCheck(Connection nats) {
    this.nats = nats;
  }

  @Override
  protected Result check() throws Exception {
    Status status = nats.getStatus();
    if (status.equals(Status.CONNECTED)) {
      return Result.healthy();
    }
    return Result.unhealthy(status.name());
  }
}
