package mck.mvnmon.nats;

import com.codahale.metrics.health.HealthCheck;
import io.nats.client.Connection;
import io.nats.client.Connection.Status;

public class NatsHealthCheck extends HealthCheck {

  private final Connection nats;

  public NatsHealthCheck(Connection nats) {
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
