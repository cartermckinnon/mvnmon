package mck.mvnmon.cmd.webhookserver;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.ResourceHelpers;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import mck.mvnmon.api.github.PushEvent;
import org.junit.jupiter.api.Test;

public class WebhookResourceTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  @Test
  public void verifyAndDeserialize() throws Exception {
    String secret = "mySecret";
    String signatureHeader =
        "sha256=b6871c41cf7cbcf8b118a3f0736329b46e119d1c7a468a18100396ea6e03e3a1";
    byte[] payload =
        Files.readAllBytes(
            new File(ResourceHelpers.resourceFilePath("fixtures/push-event.json")).toPath());

    PushEvent push =
        WebhookResource.verifyAndDeserialize(
            secret.getBytes(StandardCharsets.UTF_8), signatureHeader, payload);

    PushEvent expected = MAPPER.readValue(fixture("fixtures/push-event.json"), PushEvent.class);

    assertThat(push).isEqualTo(expected);
  }
}
