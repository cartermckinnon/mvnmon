package dev.mck.mvnmon.cmd.webhook;

import dev.mck.mvnmon.cmd.webhook.frontend.FrontendConfiguration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class WebhookConfiguration {
  @NotNull @Valid FrontendConfiguration frontend = new FrontendConfiguration();
}
