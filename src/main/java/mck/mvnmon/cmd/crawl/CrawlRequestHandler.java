package mck.mvnmon.cmd.crawl;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.MavenId;
import org.asynchttpclient.AsyncHttpClient;

/** Receives scheduled MavenId-s and initiates asynchronous lookups of their latest versions. */
@Slf4j
public class CrawlRequestHandler implements MessageHandler {

  private final AsyncHttpClient httpClient;
  private final CrawlResponseListenerFactory listenerFactory;

  public CrawlRequestHandler(
      AsyncHttpClient httpClient, CrawlResponseListenerFactory listenerFactory) {
    this.httpClient = httpClient;
    this.listenerFactory = listenerFactory;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    LOG.info("received message: '{}'", new String(msg.getData(), StandardCharsets.UTF_8));
    MavenId mavenId = MavenId.parse(msg.getData());
    String url = buildUrl(mavenId);
    httpClient
        .prepareGet(url)
        .addHeader("User-Agent", "mvnmon")
        .execute()
        .toCompletableFuture()
        .handleAsync(listenerFactory.build(mavenId), listenerFactory.getExecutor());
  }

  public static final String buildUrl(MavenId mavenId) {
    return String.format(
        "https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&start=0&rows=5",
        mavenId.getGroup(), mavenId.getArtifact());
  }
}
