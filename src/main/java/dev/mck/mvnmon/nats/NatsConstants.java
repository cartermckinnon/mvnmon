package dev.mck.mvnmon.nats;

import static java.nio.charset.StandardCharsets.UTF_8;

public enum NatsConstants {
  INSTANCE;

  public static final byte[] PING = "ping".getBytes(UTF_8);
  public static final byte[] PONG = "pong".getBytes(UTF_8);
}
