package mck.mvnmon.api.maven;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ArtifactWithId extends Artifact {

  // ignoring this field is necessary during serialization,
  // so the parent class' parse methods will succeed for both types.
  // we assume that this class never needs to be deserialized.
  @JsonIgnore private final long id;

  public ArtifactWithId(long id, String group, String artifact, List<String> versions) {
    super(group, artifact, versions);
    this.id = id;
  }
}
