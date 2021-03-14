package dev.mck.mvnmon.api.maven;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
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
}
