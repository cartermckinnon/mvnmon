package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class InstallationDeletedEvent {
  @JsonProperty("installation")
  private Installation installation;

  public Installation getInstallation() {
    return installation;
  }

  public void setInstallation(Installation installation) {
    this.installation = installation;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("installation", installation).toString();
  }
}
