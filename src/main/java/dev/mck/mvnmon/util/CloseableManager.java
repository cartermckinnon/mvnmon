package dev.mck.mvnmon.util;

import io.dropwizard.lifecycle.Managed;
import java.io.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseableManager implements Managed {

  private static final Logger LOG = LoggerFactory.getLogger(CloseableManager.class);

  private final Closeable closeable;

  public CloseableManager(Closeable closeable) {
    this.closeable = closeable;
  }

  @Override
  public void start() throws Exception {}

  @Override
  public void stop() throws Exception {
    LOG.info("closing {}...", closeable.getClass().getName());
    try {
      closeable.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
