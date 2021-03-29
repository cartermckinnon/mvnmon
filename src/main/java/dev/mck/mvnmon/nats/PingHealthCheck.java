package dev.mck.mvnmon.nats;

import static dev.mck.mvnmon.nats.NatsConstants.PING;
import static dev.mck.mvnmon.nats.NatsConstants.PONG;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.codahale.metrics.health.HealthCheck;
import dev.mck.mvnmon.util.Strings;
import io.nats.client.Connection;
import io.nats.client.Message;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingHealthCheck extends HealthCheck {

  private static final Logger LOG = LoggerFactory.getLogger(PingHealthCheck.class);

  private final String subject;
  private final Duration timeout;
  private final Connection nats;

  private final String replyTo = "ping-reply-" + Strings.random(8);
  private final BlockingQueue<Message> replies = new LinkedBlockingQueue<>();

  public PingHealthCheck(String subject, Duration timeout, Connection nats) {
    this.subject = subject;
    this.timeout = timeout;
    this.nats = nats;

    nats.createDispatcher(replies::add).subscribe(replyTo);
  }

  @Override
  protected Result check() throws Exception {
    LOG.debug("ping -> {}", subject);
    replies.clear();
    nats.publish(subject, replyTo, PING);
    var reply = replies.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    if (reply == null) {
      return Result.unhealthy(
          "no subscribers replied within " + timeout.toSeconds() + " second(s)");
    } else if (!Arrays.equals(PONG, reply.getData())) {
      return Result.unhealthy(
          "unknown response received: '" + new String(reply.getData(), UTF_8) + "'");
    }
    return Result.healthy();
  }
}
