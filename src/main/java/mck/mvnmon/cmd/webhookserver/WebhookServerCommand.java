package mck.mvnmon.cmd.webhookserver;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import mck.mvnmon.MvnMonConfiguration;
import mck.mvnmon.cmd.ExtendedServerCommand;
import mck.mvnmon.cmd.apiserver.*;

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
