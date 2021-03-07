package mck.mvnmon.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class MavenId {
  @JsonIgnore public Long id = null;

  public final String group, artifact, version;

  public MavenId(String group, String artifact, String version) {
    this(null, group, artifact, version);
  }

  public MavenId(Long id, String group, String artifact, String version) {
    this.id = id;
    this.group = group;
    this.artifact = artifact;
    this.version = version;
  }

  /**
   * @param version for example, "1.0-SNAPSHOT"
   * @return new MavenId
   */
  public MavenId withNewVersion(String version) {
    return new MavenId(id, group, artifact, version);
  }

  /**
   * @param id to set
   * @return a new MavenId
   */
  public MavenId withId(long id) {
    return new MavenId(id, group, artifact, version);
  }

  /** @return "id:group:artifact:version-classifier" */
  @Override
  public String toString() {
    var s = new StringBuilder();
    s.append(id).append(':').append(group).append(':').append(artifact).append(':').append(version);
    return s.toString();
  }

  /** @return UTF_8 representation of {@link #toString()} */
  public byte[] toBytes() {
    return toString().getBytes(StandardCharsets.UTF_8);
  }

  /**
   * @param bytes UTF_8 bytes of the form "id:group:artifact:version"
   * @return parsed MavenId
   */
  public static final MavenId parse(byte[] bytes) {
    String str = new String(bytes, StandardCharsets.UTF_8);
    String[] parts = str.split(":");
    if (parts.length != 4) {
      throw new IllegalArgumentException("malformed maven id: '" + str + "'");
    }
    return new MavenId(Long.parseLong(parts[0]), parts[1], parts[2], parts[3]);
  }
}
