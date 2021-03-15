package dev.mck.mvnmon.cmd.crawler;

import io.nats.client.Connection;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import dev.mck.mvnmon.api.maven.Artifact;

/** Builds CrawlResponseListeners and enforces a maximum on the number of concurrent requests. */
public class CrawlerResponseListenerFactory {

  private final Executor exec;
  private final Connection nats;
  private final Semaphore requestsInFlight;

  public CrawlerResponseListenerFactory(Executor exec, Connection nats, int maxConcurrentRequests) {
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
   * @param mavenArtifact
   * @return new CrawlResponseHandler
   */
  public CrawlerResponseHandler build(Artifact mavenArtifact) {
    try {
      requestsInFlight.acquire();
    } catch (InterruptedException e) {
      throw new RuntimeException(
          "failed to acquire a permit to create a crawl response handler!", e);
    }
    return new CrawlerResponseHandler(mavenArtifact, nats).setCallback(requestsInFlight::release);
  }
}
