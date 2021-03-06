package mck.mvnmon.update;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.sink.MavenIdSink;

public class UpdateMessageHandler implements MessageHandler {

  private final MavenIdSink sink;

  public UpdateMessageHandler(MavenIdSink sink) {
    this.sink = sink;
  }

  @Override
  public void onMessage(Message msg) {
    MavenId mavenId = MavenId.parse(msg.getData());
    sink.sink(mavenId);
  }
}
