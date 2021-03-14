package mck.mvnmon.cmd.webhookserver;

import javax.validation.constraints.NotBlank;
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
  @NotNull @NotBlank private String secret;
}
