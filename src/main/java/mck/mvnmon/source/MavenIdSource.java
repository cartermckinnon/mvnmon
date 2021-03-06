package mck.mvnmon.source;

import java.util.Iterator;
import mck.mvnmon.api.MavenId;

/** A source of MavenId-s. */
public interface MavenIdSource {
  public Iterator<MavenId> get();
}
