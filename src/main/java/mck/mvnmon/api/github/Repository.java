package mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
  private final String defaultBranch;

  public Repository(@JsonProperty("default_branch") String defaultBranch) {
    this.defaultBranch = defaultBranch;
  }
}
