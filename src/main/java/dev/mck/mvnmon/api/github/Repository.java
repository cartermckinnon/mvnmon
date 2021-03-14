package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Represents the "repository" of a GitHub webhook "push" event. */
@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
  private final String name;
  private final String defaultBranch;

  public Repository(
      // the "full" name is used, i.e. "mvnmon/test" instead of just "test"
      @JsonProperty("full_name") String name,
      @JsonProperty("default_branch") String defaultBranch) {
    this.name = name;
    this.defaultBranch = defaultBranch;
  }
}
