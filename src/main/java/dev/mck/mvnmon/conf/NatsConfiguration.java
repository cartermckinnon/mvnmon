package dev.mck.mvnmon.conf;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import dev.mck.mvnmon.nats.NatsFactory;
import io.dropwizard.Configuration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class NatsConfiguration extends Configuration {
  @NotNull @Valid private NatsFactory nats = new NatsFactory();

  public NatsFactory getNats() {
    return this.nats;
  }

  public void setNats(final NatsFactory nats) {
    this.nats = nats;
  }

  @Override
  public String toString() {
    return toString(MoreObjects.toStringHelper(this));
  }

  public String toString(ToStringHelper helper) {
    return helper.add("nats", nats).toString();
  }
}
