package dev.mck.mvnmon.cmd.crawler;

import io.nats.client.Connection;
import java.util.List;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.nats.Subjects;
import org.asynchttpclient.Response;

/**
 * Handles the result of a MavenArtifact crawl. If the list of latest versions has changed, a
 * notification is published to NATS.
 */
@Slf4j
public class CrawlerResponseHandler implements BiFunction<Response, Throwable, Void> {

  private final Artifact mavenArtifact;
  private final Connection nats;

  private Runnable callback = null;

  public CrawlerResponseHandler(Artifact mavenArtifact, Connection nats) {
    this.mavenArtifact = mavenArtifact;
    this.nats = nats;
  }

  @Override
  public Void apply(Response r, Throwable t) {
    try {
      if (t != null) {
        LOG.warn("exception while crawling mavenArtifact={}", mavenArtifact, t);
        // report an error with a nats message?
      } else if (r.getStatusCode() == 200) {
        String json = r.getResponseBody();
        List<String> latestVersions = CrawlerUtils.parseLatestVersionsFromResponse(json);
        if (!latestVersions.equals(mavenArtifact.getVersions())) {
          LOG.info(
              "received latestVersions={} for mavenArtifact={}", latestVersions, mavenArtifact);
          nats.publish(Subjects.CRAWLED, mavenArtifact.withVersions(latestVersions).toBytes());
        }
      }
    } finally {
      if (callback != null) {
        callback.run();
      }
    }
    return null;
  }

  public CrawlerResponseHandler setCallback(Runnable callback) {
    this.callback = callback;
    return this;
  }
}
