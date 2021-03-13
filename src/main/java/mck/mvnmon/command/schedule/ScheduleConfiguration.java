package mck.mvnmon.command.schedule;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ScheduleConfiguration {
  private int batchSize = 100;
}
