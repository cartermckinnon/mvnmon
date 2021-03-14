package dev.mck.mvnmon.cmd.webhookserver;

import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import dev.mck.mvnmon.nats.DispatcherManager;
import dev.mck.mvnmon.nats.Subjects;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class WebhookServerCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public WebhookServerCommand(Application<MvnMonConfiguration> application) {
    super(application, "webhook-server", "Webhook server to handle GitHub events.");
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    var jdbi = configuration.buildJdbi(environment);
    var nats = configuration.getNats().build(environment);
    var dispatcher = nats.createDispatcher(new WebhookMessageHandler(jdbi));
    environment.lifecycle().manage(new DispatcherManager(dispatcher));
    dispatcher.subscribe(Subjects.PUSHED, "webhook-handler");
    environment
        .jersey()
        .register(new WebhookResource(configuration.getWebhook().getSecret(), nats));
  }
}
