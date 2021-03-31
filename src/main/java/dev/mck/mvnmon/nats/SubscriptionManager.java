package dev.mck.mvnmon.nats;

import io.dropwizard.lifecycle.Managed;
import io.nats.streaming.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionManager implements Managed {

  private static final Logger LOG = LoggerFactory.getLogger(SubscriptionManager.class);

  private final Subscription s;

  public SubscriptionManager(Subscription s) {
    this.s = s;
  }

  @Override
  public void start() throws Exception {
    // no-op
  }

  @Override
  public void stop() throws Exception {
    LOG.warn(
        "closing subscription={}, queue={}, subject={}",
        s.getOptions().getDurableName(),
        s.getQueue(),
        s.getSubject());
    s.close();
  }
}
