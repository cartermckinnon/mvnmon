package mck.mvnmon.sink;

import java.io.Closeable;
import mck.mvnmon.api.MavenArtifact;

public interface MavenArtifactSink extends Closeable {
  public void sink(MavenArtifact mavenId);
}
