package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.List;

public class InstallationRepositoriesAddedEvent {
  private List<Repository> repositoriesAdded;
  private Installation installation;

  public InstallationRepositoriesAddedEvent(
      @JsonProperty("repositories_added") List<Repository> repositoriesAdded,
      @JsonProperty("installation") Installation installation) {
    this.repositoriesAdded = repositoriesAdded;
    this.installation = installation;
  }

  public List<Repository> getRepositoriesAdded() {
    return repositoriesAdded;
  }

  public Installation getInstallation() {
    return installation;
  }

  public InstallationRepositoriesAddedEvent setRepositoriesAdded(
      List<Repository> repositoriesAdded) {
    this.repositoriesAdded = repositoriesAdded;
    return this;
  }

  public InstallationRepositoriesAddedEvent setInstallation(Installation installation) {
    this.installation = installation;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("repositoriesAdded", repositoriesAdded)
        .add("installation", installation)
        .toString();
  }
}
