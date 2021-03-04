package mck.mvnmon.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class MavenId {
  public String group, artifact, version, classifier;

  public MavenId(String group, String artifact, String version, String classifier) {
    this.group = group;
    this.artifact = artifact;
    this.version = version;
    this.classifier = classifier;
  }
}
