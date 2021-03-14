package dev.mck.mvnmon.util;

import io.dropwizard.lifecycle.Managed;
import java.io.Closeable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CloseableManager implements Managed {

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
