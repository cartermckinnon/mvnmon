package mck.mvnmon.api.maven;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Dependency {
  private final String groupId;
  private final String artifactId;
  private final String version;
}
