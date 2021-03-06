package mck.mvnmon.crawl;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import mck.mvnmon.api.MavenId;
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
    MavenId mavenId = MavenId.parse(msg.getData());
    String url = buildUrl(mavenId);
    // this will block if the max concurrent flights has been reached
    var listener = listenerFactory.build(mavenId);
    httpClient
        .prepareGet(url)
        .addHeader("User-Agent", "mvnmon")
        .execute()
        .toCompletableFuture()
        .handleAsync(listener, listenerFactory.getExecutor());
  }

  public static final String buildUrl(MavenId mavenId) {
    return String.format(
        "https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&start=0&rows=5",
        mavenId.getGroup(), mavenId.getArtifact());
  }
}
