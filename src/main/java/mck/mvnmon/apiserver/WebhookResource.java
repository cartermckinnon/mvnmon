package mck.mvnmon.apiserver;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/api/v1/webhooks")
public class WebhookResource {
  @POST
  public void receive(String payload) {
    System.out.println("received a webhook=" + payload);
  }
}
