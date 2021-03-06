package mck.mvnmon.cmd.crawl;

import io.nats.client.Connection;
import java.util.concurrent.Executor;
import mck.mvnmon.api.MavenId;

public class CrawlResponseListenerFactory {

  private final Executor exec;
  private final Connection nats;

  public CrawlResponseListenerFactory(Executor exec, Connection nats) {
    this.exec = exec;
    this.nats = nats;
  }

  protected final Executor getExecutor() {
    return exec;
  }

  public CrawlResponseHandler build(MavenId mavenId) {
    return new CrawlResponseHandler(mavenId, nats);
  }
}
