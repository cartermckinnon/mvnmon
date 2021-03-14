package dev.mck.mvnmon.cmd.webhookserver;

import static java.util.stream.Collectors.toList;

import de.pdark.decentxml.Document;
import dev.mck.mvnmon.api.github.PushEvent;
import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.sql.ArtifactConsumerDao;
import dev.mck.mvnmon.sql.ArtifactDao;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.XmlFiles;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.jdbi.v3.core.Jdbi;

/**
 * Receives GitHub webhook "push" events for POM changes, creating artifact consumers accordingly.
 */
public class WebhookMessageHandler implements MessageHandler {

  private final Jdbi jdbi;

  public WebhookMessageHandler(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    PushEvent push = PushEvent.parse(msg.getData());
    List<URL> poms = push.getPomRawUrls();
    for (URL pom : poms) {
      Document doc;
      try {
        doc = XmlFiles.parseXmlFile(pom);
      } catch (IOException e) {
        throw new IllegalArgumentException("failed to parse pom=" + pom, e);
      }
      var dependencies = PomFiles.getDependencies(doc);

      // ensure that all depended-on artifacts exist in the artifacts table
      var artifacts =
          dependencies.stream()
              .map(d -> new Artifact(d.getGroupId(), d.getArtifactId(), d.getVersion()))
              .collect(toList());
      var artifactDao = jdbi.onDemand(ArtifactDao.class);
      artifactDao.insert(artifacts);

      var consumerDao = jdbi.onDemand(ArtifactConsumerDao.class);

      // remove any existing artifact consumers for this pom, in case some have been removed
      // TODO: cleanup could be done at PR time; if the artifact is no longer present in the POM
      consumerDao.delete(push.getRepository().getName(), pom.getFile());

      // insert all the consumed artifacts in this POM
      var consumers =
          dependencies.stream()
              .map(
                  d ->
                      new ArtifactConsumer(
                          push.getRepository().getName(),
                          pom.getFile(),
                          d.getGroupId(),
                          d.getArtifactId(),
                          d.getVersion()))
              .collect(toList());
      consumerDao.upsert(consumers);
    }
  }
}
