package mck.mvnmon.api.maven;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Artifact {

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

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

  /** @return a JSON representation of this MavenArtifactWithId. */
  @Override
  public String toString() {
    try {
      return MAPPER.writeValueAsString(this);
    } catch (Exception e) {
      throw new RuntimeException("failed to serialize to String", e);
    }
  }

  /** @return UTF_8 bytes of JSON representation. */
  public byte[] toBytes() {
    try {
      return MAPPER.writeValueAsBytes(this);
    } catch (Exception e) {
      throw new RuntimeException("failed to serialize to byte[]", e);
    }
  }

  public static final Artifact parse(String json) {
    try {
      return MAPPER.readValue(json, Artifact.class);
    } catch (Exception e) {
      throw new RuntimeException("failed to deserialize from json='" + json + "'", e);
    }
  }

  public static final Artifact parse(byte[] json) {
    try {
      return MAPPER.readValue(json, Artifact.class);
    } catch (Exception e) {
      throw new RuntimeException("failed to deserialize from json='" + json + "'", e);
    }
  }
}
