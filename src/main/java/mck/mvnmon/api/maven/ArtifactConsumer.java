package mck.mvnmon.api.maven;

import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ArtifactConsumer {
  private final URI uri;
  private final String groupId;
  private final String artifactId;
  private final String currentVersion;

  public ArtifactConsumer(URI uri, String groupId, String artifactId, String currentVersion) {
    this.uri = uri;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.currentVersion = currentVersion;
  }
}
