package dev.mck.mvnmon.nats;

import io.dropwizard.lifecycle.Managed;
import io.nats.streaming.StreamingConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingConnectionManager implements Managed {

  private static final Logger LOG = LoggerFactory.getLogger(StreamingConnectionManager.class);

  private final StreamingConnection conn;

  public StreamingConnectionManager(StreamingConnection conn) {
    this.conn = conn;
  }

  @Override
  public void start() throws Exception {}

  @Override
  public void stop() {
    LOG.warn("closing...");
    try {
      conn.close();
    } catch (Exception e) {
      LOG.error("failed to close!", e);
    }
  }
}
