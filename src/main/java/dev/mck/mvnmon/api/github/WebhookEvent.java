package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class WebhookEvent {
  @JsonProperty("action")
  private final String action;

  @JsonProperty("repository")
  private final Repository repository;
}
