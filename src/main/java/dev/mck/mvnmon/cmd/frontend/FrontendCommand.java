package dev.mck.mvnmon.cmd.frontend;

import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import dev.mck.mvnmon.cmd.NoopApplication;
import io.dropwizard.setup.Environment;

public class FrontendCommand extends ExtendedServerCommand<FrontendConfiguration> {

  public FrontendCommand() {
    super(
        new NoopApplication<>("frontend", FrontendConfiguration.class),
        "frontend",
        "Server to receive GitHub webhook events.");
  }

  @Override
  protected void run(Environment environment, FrontendConfiguration configuration)
      throws Exception {
    var nats = configuration.getNats().build("frontend", environment);
    environment.jersey().register(new WebhookResource(configuration.getSecret(), nats));
  }
}
