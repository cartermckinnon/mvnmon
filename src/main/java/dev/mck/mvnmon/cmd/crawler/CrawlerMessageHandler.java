package dev.mck.mvnmon.cmd.crawler;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import dev.mck.mvnmon.api.maven.Artifact;
import org.asynchttpclient.AsyncHttpClient;

/** Receives scheduled artifacts and initiates asynchronous lookups of their latest versions. */
public class CrawlerMessageHandler implements MessageHandler {

  private final AsyncHttpClient httpClient;
  private final CrawlerResponseListenerFactory listenerFactory;

  public CrawlerMessageHandler(
      AsyncHttpClient httpClient, CrawlerResponseListenerFactory listenerFactory) {
    this.httpClient = httpClient;
    this.listenerFactory = listenerFactory;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    Artifact artifact = Artifact.parse(msg.getData());
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
