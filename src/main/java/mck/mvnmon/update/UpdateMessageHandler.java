package mck.mvnmon.update;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import mck.mvnmon.api.MavenArtifactWithId;
import mck.mvnmon.sink.MavenArtifactSink;

public class UpdateMessageHandler implements MessageHandler {

  private final MavenArtifactSink sink;

  public UpdateMessageHandler(MavenArtifactSink sink) {
    this.sink = sink;
  }

  @Override
  public void onMessage(Message msg) {
    var mavenId = MavenArtifactWithId.parse(msg.getData());
    sink.sink(mavenId);
  }
}
