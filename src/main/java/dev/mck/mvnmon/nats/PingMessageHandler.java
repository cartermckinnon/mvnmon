package dev.mck.mvnmon.nats;

import static dev.mck.mvnmon.nats.NatsConstants.PING;
import static dev.mck.mvnmon.nats.NatsConstants.PONG;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import java.util.Arrays;

public class PingMessageHandler implements MessageHandler {

  private final Connection nats;

  public PingMessageHandler(Connection nats) {
    this.nats = nats;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    if (Arrays.equals(PING, msg.getData())) {
      nats.publish(msg.getReplyTo(), PONG);
    }
  }
}
