package mck.mvnmon.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class MavenArtifactWithId extends MavenArtifact {

  // ignoring this field is necessary during serialization,
  // so the parent class' parse methods will succeed for both types.
  // we assume that this class never needs to be deserialized.
  @JsonIgnore private final long id;

  public MavenArtifactWithId(long id, String group, String artifact, List<String> versions) {
    super(group, artifact, versions);
    this.id = id;
  }
}
