package dev.mck.mvnmon.cmd.frontend;

import dev.mck.mvnmon.cmd.frontend.WebhookResource;
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
        "sha256=91508dc7dcdfebe70ee32aa3bdc38b8fd45453d262508ba3bc96151dafd25f55";
    byte[] payload =
        Files.readAllBytes(
            new File(ResourceHelpers.resourceFilePath("fixtures/push-event.json")).toPath());

    WebhookResource.verifyPayload(
        secret.getBytes(StandardCharsets.UTF_8), signatureHeader, payload);
  }
}
