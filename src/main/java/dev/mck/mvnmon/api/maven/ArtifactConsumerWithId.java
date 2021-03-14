package dev.mck.mvnmon.api.maven;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ArtifactConsumerWithId extends ArtifactConsumer {

  // we assume that this class never needs to be deserialized
  @JsonIgnore private final long id;

  public ArtifactConsumerWithId(
      long id,
      String repository,
      String pom,
      String groupId,
      String artifactId,
      String currentVersion) {
    super(repository, pom, groupId, artifactId, currentVersion);
    this.id = id;
  }
}
