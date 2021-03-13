package mck.mvnmon.apiserver;

import io.nats.client.Connection;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.github.PushEvent;
import mck.mvnmon.nats.Subjects;

@Slf4j
@Path("/api/v1/webhooks")
public class WebhookResource {

  private final Connection nats;

  public WebhookResource(Connection nats) {
    this.nats = nats;
  }

  @POST
  public void receive(@NotNull PushEvent push) {
    if (push.isToDefaultBranch() && push.containsAddedorModifiedPoms()) {
      LOG.info("received push={}", push);
      nats.publish(Subjects.PUSHED, push.toBytes());
    }
  }
}
