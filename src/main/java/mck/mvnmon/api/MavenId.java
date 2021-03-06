package mck.mvnmon.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class MavenId {
  @JsonIgnore public Long id = null;

  public final String group, artifact, version, classifier;

  public MavenId(String group, String artifact, String version, String classifier) {
    this(null, group, artifact, version, classifier);
  }

  public MavenId(Long id, String group, String artifact, String version, String classifier) {
    this.id = id;
    this.group = group;
    this.artifact = artifact;
    this.version = version;
    this.classifier = classifier;
  }

  public MavenId withNewVersion(String version) {
    int classifierStart = version.indexOf("-");
    if (classifierStart > 0) {
      return new MavenId(
          id,
          group,
          artifact,
          version.substring(0, classifierStart),
          version.substring(classifierStart + 1));
    }
    return new MavenId(id, group, artifact, version, "");
  }

  @Override
  public String toString() {
    var s = new StringBuilder();
    s.append(id).append(':').append(group).append(':').append(artifact).append(':').append(version);
    if (!classifier.isEmpty()) {
      s.append('-').append(classifier);
    }
    return s.toString();
  }

  public byte[] toBytes() {
    return toString().getBytes(StandardCharsets.UTF_8);
  }

  public static final MavenId parse(byte[] bytes) {
    String str = new String(bytes, StandardCharsets.UTF_8);
    String[] parts = str.split(":");
    if (parts.length != 4) {
      throw new IllegalArgumentException("malformed maven id: '" + str + "'");
    }
    String version, classifier;
    int classifierStart = parts[3].indexOf("-");
    if (classifierStart > 0) {
      version = parts[3].substring(0, classifierStart);
      classifier = parts[3].substring(classifierStart + 1);
    } else {
      version = parts[3];
      classifier = "";
    }
    return new MavenId(Long.parseLong(parts[0]), parts[1], parts[2], version, classifier);
  }
}
