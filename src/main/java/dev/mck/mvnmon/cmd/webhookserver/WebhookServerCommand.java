package dev.mck.mvnmon.cmd.webhookserver;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.ExtendedServerCommand;

public class WebhookServerCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public WebhookServerCommand(Application<MvnMonConfiguration> application) {
    super(application, "webhook-server", "Webhook server to receive GitHub events.");
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    var nats = configuration.getNats().build(environment);
    environment
        .jersey()
        .register(new WebhookResource(configuration.getWebhook().getSecret(), nats));
  }
}
