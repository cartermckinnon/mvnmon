package mck.mvnmon.source;

import java.util.Iterator;
import mck.mvnmon.api.MavenArtifact;

/** A source of MavenId-s. */
public interface MavenArtifactSource {
  public Iterator<MavenArtifact> get();
}
