package dev.mck.mvnmon.api.maven;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Artifact {
  /*
  fields are public to allow binding via @BindFields
  */
  public final String groupId;
  public final String artifactId;
  public final List<String> versions;

  public Artifact(String groupId, String artifactId, String... versions) {
    this(groupId, artifactId, Arrays.asList(versions));
  }

  @JsonCreator
  public Artifact(
      @JsonProperty("groupId") String groupId,
      @JsonProperty("artifactId") String artifactId,
      @JsonProperty("versions") List<String> versions) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.versions = versions;
  }

  /**
   * @param versions for example, "1.0-SNAPSHOT"
   * @return new MavenArtifact
   */
  public Artifact withVersions(String... versions) {
    return withVersions(Arrays.asList(versions));
  }

  /**
   * @param versions for example, "1.0-SNAPSHOT"
   * @return new MavenArtifact
   */
  public Artifact withVersions(List<String> versions) {
    return new Artifact(groupId, artifactId, versions);
  }

  @Override
  public String toString() {
    return toString(MoreObjects.toStringHelper(this));
  }

  public String toString(ToStringHelper helper) {
    return helper
        .add("groupId", groupId)
        .add("artifactId", artifactId)
        .add("versions", versions)
        .toString();
  }

  public String getGroupId() {
    return this.groupId;
  }

  public String getArtifactId() {
    return this.artifactId;
  }

  public List<String> getVersions() {
    return this.versions;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 29 * hash + Objects.hashCode(this.groupId);
    hash = 29 * hash + Objects.hashCode(this.artifactId);
    hash = 29 * hash + Objects.hashCode(this.versions);
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
    final Artifact other = (Artifact) obj;
    if (!Objects.equals(this.groupId, other.groupId)) {
      return false;
    }
    if (!Objects.equals(this.artifactId, other.artifactId)) {
      return false;
    }
    return Objects.equals(this.versions, other.versions);
  }
}
