package dev.mck.mvnmon.cmd.frontend;

import com.google.common.base.MoreObjects;
import dev.mck.mvnmon.conf.NatsConfiguration;

public class FrontendConfiguration extends NatsConfiguration {
  private String secret = null;

  /** If null, webhook payloads will not be verified. */
  public String getSecret() {
    return this.secret;
  }

  /** If the secret is null, webhook payloads will not be verified. */
  public void setSecret(final String secret) {
    this.secret = secret;
  }

  @Override
  public String toString() {
    return super.toString(
        MoreObjects.toStringHelper(this)
            .add("secret", secret == null ? "null" : "len(" + secret.length() + ")"));
  }
}
