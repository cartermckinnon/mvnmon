package dev.mck.mvnmon.cmd.backend.crawler;

import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.nats.Subjects;
import dev.mck.mvnmon.util.Serialization;
import io.nats.client.Connection;
import java.util.List;
import java.util.function.BiFunction;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the result of a MavenArtifact crawl. If the list of latest versions has changed, a
 * notification is published to NATS.
 */
public class CrawlerResponseHandler implements BiFunction<Response, Throwable, Void> {

  private static final Logger LOG = LoggerFactory.getLogger(CrawlerResponseHandler.class);

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
      } else
      // report an error with a nats message?
      if (r.getStatusCode() == 200) {
        String json = r.getResponseBody();
        List<String> latestVersions = CrawlerUtils.parseLatestVersionsFromResponse(json);
        if (!latestVersions.equals(mavenArtifact.getVersions())) {
          LOG.info(
              "received latestVersions={} for mavenArtifact={}", latestVersions, mavenArtifact);
          Artifact updatedArtifact = mavenArtifact.withVersions(latestVersions);
          nats.publish(Subjects.CRAWLED, Serialization.serializeAsBytes(updatedArtifact));
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
