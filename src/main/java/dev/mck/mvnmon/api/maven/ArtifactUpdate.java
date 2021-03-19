package dev.mck.mvnmon.api.maven;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class ArtifactUpdate {
  private final String groupId;
  private final String artifactId;
  private final String currentVersion;
  private final String newVersion;

  public ArtifactUpdate(
      String groupId, String artifactId, String currentVersion, String newVersion) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.currentVersion = currentVersion;
    this.newVersion = newVersion;
  }

  public String getGroupId() {
    return this.groupId;
  }

  public String getArtifactId() {
    return this.artifactId;
  }

  public String getCurrentVersion() {
    return this.currentVersion;
  }

  public String getNewVersion() {
    return this.newVersion;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("groupId", groupId)
        .add("artifactId", artifactId)
        .add("currentVersion", currentVersion)
        .add("newVersion", newVersion)
        .toString();
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 83 * hash + Objects.hashCode(this.groupId);
    hash = 83 * hash + Objects.hashCode(this.artifactId);
    hash = 83 * hash + Objects.hashCode(this.currentVersion);
    hash = 83 * hash + Objects.hashCode(this.newVersion);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ArtifactUpdate other = (ArtifactUpdate) obj;
    if (!Objects.equals(this.groupId, other.groupId)) {
      return false;
    }
    if (!Objects.equals(this.artifactId, other.artifactId)) {
      return false;
    }
    if (!Objects.equals(this.currentVersion, other.currentVersion)) {
      return false;
    }
    return Objects.equals(this.newVersion, other.newVersion);
  }
}
