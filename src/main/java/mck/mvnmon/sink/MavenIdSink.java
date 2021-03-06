package mck.mvnmon.sink;

import java.io.Closeable;
import mck.mvnmon.api.MavenId;

public interface MavenIdSink extends Closeable {
  public void sink(MavenId mavenId);
}
