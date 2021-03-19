package dev.mck.mvnmon.api.maven;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;

public class ArtifactConsumer {
  /*
  fields are public to allow binding via @BindFields
  */
  public final long pomId;
  public final String groupId;
  public final String artifactId;
  public final String currentVersion;

  public ArtifactConsumer(long pomId, String groupId, String artifactId, String currentVersion) {
    this.pomId = pomId;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.currentVersion = currentVersion;
  }

  public long getPomId() {
    return this.pomId;
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

  @Override
  public String toString() {
    return toString(MoreObjects.toStringHelper(this));
  }

  public String toString(ToStringHelper helper) {
    return helper
        .add("pomId", pomId)
        .add("groupId", groupId)
        .add("artifactId", artifactId)
        .add("currentVersion", currentVersion)
        .toString();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + (int) (this.pomId ^ (this.pomId >>> 32));
    hash = 53 * hash + Objects.hashCode(this.groupId);
    hash = 53 * hash + Objects.hashCode(this.artifactId);
    hash = 53 * hash + Objects.hashCode(this.currentVersion);
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
    final ArtifactConsumer other = (ArtifactConsumer) obj;
    if (this.pomId != other.pomId) {
      return false;
    }
    if (!Objects.equals(this.groupId, other.groupId)) {
      return false;
    }
    if (!Objects.equals(this.artifactId, other.artifactId)) {
      return false;
    }
    return Objects.equals(this.currentVersion, other.currentVersion);
  }
}
