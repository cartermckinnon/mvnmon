package mck.mvnmon.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class MavenDependency {
  private final String groupId;
  private final String artifactId;
  private final String version;
}
