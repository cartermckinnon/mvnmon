package dev.mck.mvnmon.cmd.webhook.frontend;

import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class FrontendCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public FrontendCommand(Application<MvnMonConfiguration> application) {
    super(application, "frontend", "Webhook server to receive GitHub events.");
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    var nats = configuration.getNats().build(environment);
    environment
        .jersey()
        .register(new WebhookResource(configuration.getWebhook().getFrontend().getSecret(), nats));
  }
}
