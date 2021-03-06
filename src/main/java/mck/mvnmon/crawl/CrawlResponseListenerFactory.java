package mck.mvnmon.crawl;

import io.nats.client.Connection;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import mck.mvnmon.api.MavenId;

/** Builds CrawlResponseListeners and enforces a maximum on the number of concurrent requests. */
public class CrawlResponseListenerFactory {

  private final Executor exec;
  private final Connection nats;
  private final Semaphore requestsInFlight;

  public CrawlResponseListenerFactory(Executor exec, Connection nats, int maxConcurrentRequests) {
    this.exec = exec;
    this.nats = nats;
    this.requestsInFlight = new Semaphore(maxConcurrentRequests);
  }

  protected final Executor getExecutor() {
    return exec;
  }

  /**
   * Build a CrawlResponseHandler, blocking until a permit is available (i.e. the number of
   * concurrent requests is less than the maximum allowed).
   *
   * @param mavenId
   * @return new CrawlResponseHandler
   */
  public CrawlResponseHandler build(MavenId mavenId) {
    try {
      requestsInFlight.acquire();
    } catch (InterruptedException e) {
      throw new RuntimeException(
          "failed to acquire a permit to create a crawl response handler!", e);
    }
    return new CrawlResponseHandler(mavenId, nats).setCallback(requestsInFlight::release);
  }
}
