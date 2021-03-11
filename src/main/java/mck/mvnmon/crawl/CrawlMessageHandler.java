package mck.mvnmon.crawl;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import mck.mvnmon.api.MavenArtifact;
import org.asynchttpclient.AsyncHttpClient;

/** Receives scheduled MavenId-s and initiates asynchronous lookups of their latest versions. */
public class CrawlMessageHandler implements MessageHandler {

  private final AsyncHttpClient httpClient;
  private final CrawlResponseListenerFactory listenerFactory;

  public CrawlMessageHandler(
      AsyncHttpClient httpClient, CrawlResponseListenerFactory listenerFactory) {
    this.httpClient = httpClient;
    this.listenerFactory = listenerFactory;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    MavenArtifact artifact = MavenArtifact.parse(msg.getData());
    String url = CrawlUtils.buildUrl(artifact.getGroupId(), artifact.getArtifactId());
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
