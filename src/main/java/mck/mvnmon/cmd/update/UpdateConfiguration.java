package mck.mvnmon.cmd.update;

import io.dropwizard.util.Duration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UpdateConfiguration {
  private int batchSize = 100;
  private Duration interval = Duration.seconds(10);
}
