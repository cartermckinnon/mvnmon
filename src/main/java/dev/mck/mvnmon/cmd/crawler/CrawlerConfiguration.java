package dev.mck.mvnmon.cmd.crawler;

import javax.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CrawlerConfiguration {
  @Min(1)
  private int maxConcurrentRequests = 32;
}
