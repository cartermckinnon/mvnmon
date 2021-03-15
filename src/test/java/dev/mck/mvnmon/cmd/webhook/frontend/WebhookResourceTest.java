package dev.mck.mvnmon.cmd.webhook.frontend;

import io.dropwizard.testing.ResourceHelpers;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

public class WebhookResourceTest {

  @Test
  public void verifyPayload() throws Exception {
    String secret = "mySecret";
    String signatureHeader =
        "sha256=b6871c41cf7cbcf8b118a3f0736329b46e119d1c7a468a18100396ea6e03e3a1";
    byte[] payload =
        Files.readAllBytes(
            new File(ResourceHelpers.resourceFilePath("fixtures/push-event.json")).toPath());

    WebhookResource.verifyPayload(
        secret.getBytes(StandardCharsets.UTF_8), signatureHeader, payload);
  }
}
