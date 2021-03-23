package dev.mck.mvnmon.cmd.backend.crawler;

import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.nats.TypedHandler;
import org.asynchttpclient.AsyncHttpClient;

/** Receives scheduled artifacts and initiates asynchronous lookups of their latest versions. */
public class CrawlerMessageHandler extends TypedHandler<Artifact> {

  private final AsyncHttpClient httpClient;
  private final CrawlerResponseListenerFactory listenerFactory;

  public CrawlerMessageHandler(
      AsyncHttpClient httpClient, CrawlerResponseListenerFactory listenerFactory) {
    super(Artifact.class);
    this.httpClient = httpClient;
    this.listenerFactory = listenerFactory;
  }

  @Override
  protected void handlePayload(Artifact artifact) {
    String url = CrawlerUtils.buildUrl(artifact.getGroupId(), artifact.getArtifactId());
    // this will block if the max concurrent flights has been reached
    var listener = listenerFactory.build(artifact);
    httpClient
        .prepareGet(url)
        .addHeader("User-Agent", "mvnmon")
        .execute()
        .toCompletableFuture()
        .handleAsync(listener, listenerFactory.getExecutor());
  }
}
