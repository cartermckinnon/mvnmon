package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
  private final String login;

  @JsonCreator
  public Account(@JsonProperty("login") String login) {
    this.login = login;
  }

  public String getLogin() {
    return login;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("login", login).toString();
  }
}
