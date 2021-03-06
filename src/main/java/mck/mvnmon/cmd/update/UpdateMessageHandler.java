package mck.mvnmon.cmd.update;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import mck.mvnmon.api.MavenId;

public class UpdateMessageHandler implements MessageHandler {

  private final UpdateBatcher updateBatcher;

  public UpdateMessageHandler(UpdateBatcher updateBatcher) {
    this.updateBatcher = updateBatcher;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    MavenId mavenId = MavenId.parse(msg.getData());
    updateBatcher.queueMavenId(mavenId);
  }
}
