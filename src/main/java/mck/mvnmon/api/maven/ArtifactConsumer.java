package mck.mvnmon.api.maven;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ArtifactConsumer {

  /*
  fields are public to allow binding via @BindFields
  */

  public final String repository;
  public final String pom;
  public final String groupId;
  public final String artifactId;
  public final String currentVersion;

  public ArtifactConsumer(
      String repository, String pom, String groupId, String artifactId, String currentVersion) {
    this.repository = repository;
    this.pom = pom;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.currentVersion = currentVersion;
  }
}
