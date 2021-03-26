package dev.mck.mvnmon.cmd.frontend;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.junit.jupiter.api.Test;

public class WebhookResourceTest {

  @Test
  public void verifyPayload() throws Exception {
    byte[] secret = "mySecret".getBytes(UTF_8);
    String signatureHeader =
        "sha256=633e18e8c90bc8b56c127f5cd3748a43e34134d8d1113de59e8fe105fc61c8e9";
    byte[] payload = "hello, world".getBytes(UTF_8);
    WebhookResource.verifyPayload(secret, signatureHeader, payload);
  }
}
