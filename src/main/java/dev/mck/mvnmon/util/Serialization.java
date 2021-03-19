package dev.mck.mvnmon.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.io.IOException;

public class Serialization {
  public static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  public static final <T> T deserialize(byte[] json, Class<T> clazz) {
    try {
      return MAPPER.readValue(json, clazz);
    } catch (IOException e) {
      throw new RuntimeException("failed to deserialize class=" + clazz, e);
    }
  }

  public static final byte[] serializeAsBytes(Object o) {
    try {
      return MAPPER.writeValueAsBytes(o);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("failed to serialize as bytes!", e);
    }
  }
}
