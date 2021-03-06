package mck.mvnmon.cmd.crawl;

import com.jayway.jsonpath.JsonPath;
import io.nats.client.Connection;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.ipc.Subjects;
import org.asynchttpclient.Response;

@Slf4j
public class CrawlResponseHandler implements BiFunction<Response, Throwable, Void> {

  private final MavenId mavenId;
  private final Connection nats;

  public CrawlResponseHandler(MavenId mavenId, Connection nats) {
    this.mavenId = mavenId;
    this.nats = nats;
  }

  @Override
  public Void apply(Response r, Throwable t) {
    if (t != null) {
      LOG.warn("exception while crawling mavenId={}", mavenId, t);
      // report an error with a nats message?
    } else if (r.getStatusCode() == 200) {
      String json = r.getResponseBody();
      Object versionElement = JsonPath.read(json, "$.response.docs[0].latestVersion");
      if (versionElement instanceof String) {
        LOG.warn("received version={} for mavenId={}", versionElement, mavenId);
        String version = (String) versionElement;
        if (!version.equals(mavenId.getVersion())) {
          nats.publish(Subjects.CRAWLED, mavenId.withNewVersion(version).toBytes());
        }
      } else if (versionElement != null) {
        LOG.warn(
            "unexpected type={} when parsing version={} for mavenId={} in response={}",
            versionElement.getClass().getName(),
            versionElement,
            mavenId,
            json);
      } else {
        LOG.warn("no version for mavenId={} in response={}", mavenId, json);
      }
    }
    return null;
  }
}
