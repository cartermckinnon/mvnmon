package dev.mck.mvnmon.api.maven;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import java.util.List;

public class ArtifactWithId extends Artifact {
  // we assume that this class never needs to be deserialized.
  @JsonIgnore private final long id;

  public ArtifactWithId(long id, String group, String artifact, List<String> versions) {
    super(group, artifact, versions);
    this.id = id;
  }

  public long getId() {
    return this.id;
  }

  @Override
  public String toString() {
    return super.toString(MoreObjects.toStringHelper(this).add("id", id));
  }

  @Override
  public int hashCode() {
    int hash = super.hashCode();
    hash = 59 * hash + (int) (this.id ^ (this.id >>> 32));
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
    final ArtifactWithId other = (ArtifactWithId) obj;
    if (this.id != other.id) {
      return false;
    }
    return super.equals(obj);
  }
}
