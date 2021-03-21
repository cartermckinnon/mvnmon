package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.List;

public class InstallationRepositoriesRemovedEvent {
  private List<Repository> repositoriesRemoved;
  private Installation installation;

  public InstallationRepositoriesRemovedEvent(
      @JsonProperty("repositories_removed") List<Repository> repositoriesRemoved,
      @JsonProperty("installation") Installation installation) {
    this.repositoriesRemoved = repositoriesRemoved;
    this.installation = installation;
  }

  public List<Repository> getRepositoriesRemoved() {
    return repositoriesRemoved;
  }

  public Installation getInstallation() {
    return installation;
  }

  public InstallationRepositoriesRemovedEvent setRepositoriesRemoved(
      List<Repository> repositoriesRemoved) {
    this.repositoriesRemoved = repositoriesRemoved;
    return this;
  }

  public InstallationRepositoriesRemovedEvent setInstallation(Installation installation) {
    this.installation = installation;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("repositoriesRemoved", repositoriesRemoved)
        .add("installation", installation)
        .toString();
  }
}
