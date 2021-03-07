package mck.mvnmon.crawl;

import io.nats.client.Connection;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.nats.Subjects;
import org.asynchttpclient.Response;

/**
 * Handles the result of a Maven ID crawl. If the latest version has changed, a notification is
 * published to NATS.
 */
@Slf4j
public class CrawlResponseHandler implements BiFunction<Response, Throwable, Void> {

  private final MavenId mavenId;
  private final Connection nats;

  private Runnable callback = null;

  public CrawlResponseHandler(MavenId mavenId, Connection nats) {
    this.mavenId = mavenId;
    this.nats = nats;
  }

  @Override
  public Void apply(Response r, Throwable t) {
    try {
      if (t != null) {
        LOG.warn("exception while crawling mavenId={}", mavenId, t);
        // report an error with a nats message?
      } else if (r.getStatusCode() == 200) {
        String json = r.getResponseBody();
        Optional<String> newVersion = CrawlUtils.parseNewVersionFromResponse(json, mavenId);
        if (newVersion.isPresent()) {
          LOG.info("received new version={} for mavenId={}", newVersion.get(), mavenId);
          nats.publish(Subjects.CRAWLED, mavenId.withNewVersion(newVersion.get()).toBytes());
        }
      }
    } finally {
      if (callback != null) {
        callback.run();
      }
    }
    return null;
  }

  public CrawlResponseHandler setCallback(Runnable callback) {
    this.callback = callback;
    return this;
  }
}
