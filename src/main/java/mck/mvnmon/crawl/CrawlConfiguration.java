package mck.mvnmon.crawl;

import javax.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CrawlConfiguration {
  @Min(1)
  private int maxConcurrentRequests = 32;
}
