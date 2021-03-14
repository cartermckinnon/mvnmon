package mck.mvnmon.cmd.webhookserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import io.dropwizard.jackson.Jackson;
import io.nats.client.Connection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.github.PushEvent;
import mck.mvnmon.nats.Subjects;

/** Receives GitHub webhook "push" events. */
@Slf4j
@Path("/api/v1/webhooks")
public class WebhookResource {

  private final Connection nats;
  private final byte[] secret;

  public WebhookResource(String secret, Connection nats) {
    this.nats = nats;
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Publishes to NATS if a push was made to the default branch, and its commits contain POM files.
   *
   * @param signatureHeader
   * @param payload
   */
  @POST
  public void receive(@HeaderParam("X-Hub-Signature-256") String signatureHeader, byte[] payload) {
    PushEvent push = verifyAndDeserialize(secret, signatureHeader, payload);
    if (push.isToDefaultBranch() && push.containsAddedOrModifiedPoms()) {
      LOG.info("received push={}", push);
      nats.publish(Subjects.PUSHED, push.toBytes());
    }
  }

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  protected static PushEvent verifyAndDeserialize(
      byte[] secret, String signatureHeader, byte[] payload) {
    // cut off "sha256=" from the header value
    HashCode signatureHashCode = HashCode.fromString(signatureHeader.substring(7));
    HashCode hashCode = Hashing.hmacSha256(secret).hashBytes(payload);
    if (!hashCode.equals(signatureHashCode)) {
      throw new IllegalArgumentException("signature of request is invalid!");
    }
    try {
      return MAPPER.readValue(payload, PushEvent.class);
    } catch (IOException e) {
      throw new IllegalArgumentException("failed to deserialize payload!", e);
    }
  }
}
