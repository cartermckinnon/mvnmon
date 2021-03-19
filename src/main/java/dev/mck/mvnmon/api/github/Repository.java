package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/** Represents the "repository" of a GitHub webhook "push" event. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
  @JsonProperty("full_name")
  private final String name;

  @JsonProperty("default_branch")
  private final String defaultBranch;

  @JsonProperty("id")
  private final long id;

  @JsonCreator
  public Repository(
      // the "full" name is used, i.e. "mvnmon/test" instead of just "test"
      @JsonProperty("full_name") String name,
      @JsonProperty("default_branch") String defaultBranch,
      @JsonProperty("id") long id) {
    this.name = name;
    this.defaultBranch = defaultBranch;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getDefaultBranch() {
    return defaultBranch;
  }

  public long getId() {
    return id;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("defaultBranch", defaultBranch)
        .add("id", id)
        .toString();
  }
}
