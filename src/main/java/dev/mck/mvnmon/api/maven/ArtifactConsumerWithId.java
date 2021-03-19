package dev.mck.mvnmon.api.maven;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

public class ArtifactConsumerWithId extends ArtifactConsumer {
  // we assume that this class never needs to be deserialized
  @JsonIgnore private final long id;

  public ArtifactConsumerWithId(
      long id, long pomId, String groupId, String artifactId, String currentVersion) {
    super(pomId, groupId, artifactId, currentVersion);
    this.id = id;
  }

  public long getId() {
    return this.id;
  }

  @Override
  public String toString() {
    return super.toString(MoreObjects.toStringHelper(this).add("id", id));
  }
}
