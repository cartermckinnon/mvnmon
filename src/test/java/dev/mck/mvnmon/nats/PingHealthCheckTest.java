package dev.mck.mvnmon.nats;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;

public class PingHealthCheckTest extends NatsTest {
  @Test
  public void oneClient() {
    String subject = "one-client-ping-test";
    getConnection()
        .createDispatcher(msg -> getConnection().publish(msg.getReplyTo(), NatsConstants.PONG))
        .subscribe(subject);

    var hc = new PingHealthCheck(subject, Duration.ofSeconds(10), getConnection());

    var result = hc.execute();

    assertThat(result.isHealthy()).isTrue();
  }
}
