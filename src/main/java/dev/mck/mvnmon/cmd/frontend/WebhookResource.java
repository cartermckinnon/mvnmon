package dev.mck.mvnmon.cmd.frontend;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.jayway.jsonpath.JsonPath;
import dev.mck.mvnmon.nats.Subjects;
import io.nats.client.Connection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Receives GitHub webhook events. */
@Path("/webhooks")
public class WebhookResource {

  private static final Logger LOG = LoggerFactory.getLogger(WebhookResource.class);

  private final Connection nats;
  private final byte[] secret;

  public WebhookResource(String secret, Connection nats) {
    this.nats = nats;
    this.secret = secret == null ? null : secret.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Forwards (valid) webhook events to NATS.
   *
   * @param signatureHeader
     * @param guid
   * @param payload
     * @param event
   */
  @POST
  public void receive(
      @HeaderParam("X-Hub-Signature-256") String signatureHeader,
      @NotNull @HeaderParam("X-GitHub-Delivery") String guid,
      @NotNull @HeaderParam("X-GitHub-Event") String event,
      @NotNull byte[] payload) {
    LOG.info("guid={}, event={}", guid, event);
    verifyPayload(secret, signatureHeader, payload);
    String subject = switch (event) {
      case "push" -> Subjects.hook(event);
      case "installation", "installation_repositories" -> Subjects.hook(event, getActionFromPayload(payload));
      default -> null;
    };
    if(subject != null) {
        nats.publish(subject, payload);
    }
  }

  protected static void verifyPayload(byte[] secret, String signatureHeader, byte[] payload) {
    if (secret != null) {
      if (signatureHeader == null) {
        throw new IllegalArgumentException("signature header cannot be null!");
      }
      // cut off "sha256=" from the header value
      HashCode signatureHashCode = HashCode.fromString(signatureHeader.substring(7));
      HashCode hashCode = Hashing.hmacSha256(secret).hashBytes(payload);
      if (!hashCode.equals(signatureHashCode)) {
        throw new IllegalArgumentException("signature is invalid!");
      }
    }
  }

  protected static String getActionFromPayload(byte[] payload) {
    String action;
    try {
      action = JsonPath.read(new ByteArrayInputStream(payload), "$.action");
    } catch (IOException e) {
      throw new RuntimeException("failed to get action from payload!", e);
    }
    if (action == null) {
      throw new IllegalArgumentException("payload does not contain action!");
    }
    return action;
  }
}
