package dev.mck.mvnmon.cmd.webhook.frontend;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class FrontendConfiguration {

  /** If a secret isn't defined, webhook payloads will not be verified. */
  private String secret = null;
}
