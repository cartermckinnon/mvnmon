package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Represents an "author" or "committer" of a GitHub webhook "push" event. */
@Getter
@ToString
@EqualsAndHashCode
public class Author {
  private final String name;
  private final String email;
  private final String username;

  public Author(
      @JsonProperty("name") String name,
      @JsonProperty("email") String email,
      @JsonProperty("username") String username) {
    this.name = name;
    this.email = email;
    this.username = username;
  }
}
