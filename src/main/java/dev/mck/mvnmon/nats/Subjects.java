package dev.mck.mvnmon.nats;

public class Subjects {
  public static final String SCHEDULED = "scheduled";
  public static final String CRAWLED = "crawled";
  public static final String PUSHED = "pushed";
  public static final String BACKEND_PING = "backend-ping";

  public static final String hook(String action) {
    return "hook-" + action;
  }

  public static final String hook(String event, String action) {
    return String.join("-", event, action);
  }
}
