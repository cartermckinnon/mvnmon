package mck.mvnmon.ipc;

import io.dropwizard.lifecycle.Managed;
import io.nats.client.Subscription;
import java.time.Duration;

public class SubscriptionManager implements Managed {

  private final Subscription subscription;

  public SubscriptionManager(Subscription subscription) {
    this.subscription = subscription;
  }

  @Override
  public void start() throws Exception {}

  @Override
  public void stop() throws Exception {
    subscription.drain(Duration.ZERO).get();
  }
}
