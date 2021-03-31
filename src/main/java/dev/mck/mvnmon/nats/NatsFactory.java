package dev.mck.mvnmon.nats;

import com.google.common.base.MoreObjects;
import com.google.common.net.HostAndPort;
import io.dropwizard.setup.Environment;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
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

  public StreamingConnection build(String clientId) throws Exception {
    var options =
        new Options.Builder()
            .clusterId("mvnmon")
            .clientId(clientId)
            .natsUrl(concatenateServers())
            .build();
    var factory = new StreamingConnectionFactory();
    factory.setOptions(options);
    return factory.createConnection();
  }

  public StreamingConnection build(String clientId, Environment e) throws Exception {
    var streamingConn = build(clientId);
    e.lifecycle().manage(new StreamingConnectionManager(streamingConn));
    e.healthChecks().register("nats", new ConnectionHealthCheck(streamingConn.getNatsConnection()));
    return streamingConn;
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
