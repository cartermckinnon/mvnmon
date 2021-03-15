package dev.mck.mvnmon.cmd.webhook.frontend;

import javax.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class FrontendConfiguration {
  @NotBlank private String secret = null;
}
