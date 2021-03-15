package dev.mck.mvnmon.cmd.pullrequester;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class PullRequesterConfiguration {
  @Min(10)
  @Max(100)
  private int batchSize = 100;

  /** An access key is always required; we won't be able to commit to a repository anonymously */
  @NotNull @NotBlank private String accessKey;
}
