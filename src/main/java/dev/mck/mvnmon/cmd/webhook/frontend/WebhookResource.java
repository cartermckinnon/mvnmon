package dev.mck.mvnmon.cmd.webhook.frontend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import dev.mck.mvnmon.nats.Subjects;
import io.dropwizard.jackson.Jackson;
import io.nats.client.Connection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

/** Receives GitHub webhook "push" events. */
@Slf4j
@Path("/api/webhooks")
public class WebhookResource {

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
   * @param payload
   */
  @POST
  public void receive(
      @HeaderParam("X-Hub-Signature-256") String signatureHeader,
      @NotNull @HeaderParam("X-GitHub-Delivery") String guid,
      @NotNull @HeaderParam("X-GitHub-Event") String event,
      @NotNull byte[] payload) {
    LOG.info("guid={}, event={}", guid, event);
    verifyPayload(secret, signatureHeader, payload);
    nats.publish(Subjects.hook(event), payload);
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

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  protected static <T> T deserializePayload(byte[] payload, Class<T> clazz) {
    try {
      return MAPPER.readValue(payload, clazz);
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "failed to deserialize payload to class=" + clazz.getName(), e);
    }
  }

  protected static byte[] serializePayload(Object payload) {
    try {
      return MAPPER.writeValueAsBytes(payload);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(
          "failed to serialize payload of class=" + payload.getClass().getName(), e);
    }
  }
}
