package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.List;

public class InstallationCreatedEvent {

  private final String action;
  private final List<Repository> repositories;
  private final Installation installation;

  @JsonCreator
  public InstallationCreatedEvent(
      @JsonProperty("action") String action,
      @JsonProperty("repositories") List<Repository> repositories,
      @JsonProperty("installation") Installation installation) {
    this.action = action;
    this.repositories = repositories;
    this.installation = installation;
  }

  public String getAction() {
    return action;
  }

  public List<Repository> getRepositories() {
    return repositories;
  }

  public Installation getInstallation() {
    return installation;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("action", action)
        .add("repositories", repositories)
        .add("installation", installation)
        .toString();
  }
}
