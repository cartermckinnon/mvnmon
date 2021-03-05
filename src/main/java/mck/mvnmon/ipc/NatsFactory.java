package mck.mvnmon.ipc;

import io.dropwizard.setup.Environment;
import io.nats.client.Connection;
import io.nats.client.Nats;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class NatsFactory {
  private String[] urls = {"nats://localhost:4222"};

  public String concatenateUrls() {
    return String.join(",", urls);
  }

  public Connection build() throws Exception {
    return Nats.connect(concatenateUrls());
  }

  public Connection build(Environment e) throws Exception {
    var nats = build();
    e.lifecycle().manage(new NatsManager(nats));
    return nats;
  }
}
