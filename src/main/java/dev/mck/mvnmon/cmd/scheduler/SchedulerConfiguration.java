package dev.mck.mvnmon.cmd.scheduler;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class SchedulerConfiguration {
  private int batchSize = 100;
}
