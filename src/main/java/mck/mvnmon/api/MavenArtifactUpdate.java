package mck.mvnmon.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class MavenArtifactUpdate {
  private final String groupId;
  private final String artifactId;
  private final String currentVersion;
  private final String newVersion;

  public MavenArtifactUpdate(
      String groupId, String artifactId, String currentVersion, String newVersion) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.currentVersion = currentVersion;
    this.newVersion = newVersion;
  }
}
