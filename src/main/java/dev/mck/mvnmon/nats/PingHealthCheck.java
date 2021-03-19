package dev.mck.mvnmon.nats;

import static dev.mck.mvnmon.nats.NatsConstants.PING;
import static dev.mck.mvnmon.nats.NatsConstants.PONG;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.codahale.metrics.health.HealthCheck;
import io.nats.client.Connection;
import java.time.Duration;
import java.util.Arrays;

public class PingHealthCheck extends HealthCheck {

  private final String subject;
  private final Duration timeout;
  private final Connection nats;

  public PingHealthCheck(String subject, Duration timeout, Connection nats) {
    this.subject = subject;
    this.timeout = timeout;
    this.nats = nats;
  }

  @Override
  protected Result check() throws Exception {
    var reply = nats.request(subject + "-ping", PING, timeout);
    if (reply == null) {
      return Result.unhealthy("no subscribers replied within " + timeout + " second(s)");
    } else if (!Arrays.equals(PONG, reply.getData())) {
      return Result.unhealthy(
          "unknown response received: '" + new String(reply.getData(), UTF_8) + "'");
    }
    return Result.healthy();
  }
}
