package mck.mvnmon.nats;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class NatsConfiguration {
  private String[] urls = {"nats://localhost:4222"};

  public String concatenateUrls() {
    return String.join(",", urls);
  }
}
