package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Installation {
  private final long id;
  private final Account account;

  @JsonCreator
  public Installation(@JsonProperty("id") long id, @JsonProperty("account") Account account) {
    this.id = id;
    this.account = account;
  }

  public long getId() {
    return id;
  }

  public Account getAccount() {
    return account;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("account", account).toString();
  }
}
