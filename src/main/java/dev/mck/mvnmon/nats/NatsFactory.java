package dev.mck.mvnmon.nats;

import com.google.common.base.MoreObjects;
import com.google.common.net.HostAndPort;
import io.dropwizard.setup.Environment;
import io.nats.client.Connection;
import io.nats.client.Nats;
import java.util.Collections;
import java.util.Set;

public class NatsFactory {
  private Set<HostAndPort> servers =
      Collections.singleton(HostAndPort.fromString("localhost:4222"));

  public String concatenateServers() {
    var res = new StringBuilder();
    for (HostAndPort server : servers) {
      if (!res.isEmpty()) {
        res.append(',');
      }
      res.append(server);
    }
    return res.toString();
  }

  public Connection build() throws Exception {
    return Nats.connect(concatenateServers());
  }

  public Connection build(Environment e) throws Exception {
    var nats = build();
    e.lifecycle().manage(new NatsManager(nats));
    e.healthChecks().register("nats", new NatsHealthCheck(nats));
    return nats;
  }

  public Set<HostAndPort> getServers() {
    return this.servers;
  }

  public void setServers(final Set<HostAndPort> servers) {
    this.servers = servers;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("servers", servers).toString();
  }
}
