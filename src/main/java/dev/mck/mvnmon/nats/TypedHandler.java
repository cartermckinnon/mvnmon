package dev.mck.mvnmon.nats;

import dev.mck.mvnmon.util.Serialization;
import io.nats.streaming.Message;
import io.nats.streaming.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TypedHandler<T> implements MessageHandler {

  private static final Logger LOG = LoggerFactory.getLogger(TypedHandler.class);

  private final Class<T> payloadClass;

  public TypedHandler(Class<T> payloadClass) {
    this.payloadClass = payloadClass;
  }

  @Override
  public final void onMessage(Message msg) {
    T payload = Serialization.deserialize(msg.getData(), payloadClass);
    try {
      handlePayload(payload);
    } catch (Exception e) {
      LOG.error("failed to handle payload={}", payloadClass.getName(), e);
    }
  }

  protected abstract void handlePayload(T payload) throws Exception;
}
