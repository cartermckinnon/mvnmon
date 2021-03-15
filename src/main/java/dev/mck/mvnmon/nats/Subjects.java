package dev.mck.mvnmon.nats;

public class Subjects {
  public static final String SCHEDULED = "scheduled";
  public static final String CRAWLED = "crawled";
  public static final String PUSHED = "pushed";

  public static final String hook(String event) {
    return "hook-" + event;
  }
}
