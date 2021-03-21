package dev.mck.mvnmon.cmd.frontend;

import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import dev.mck.mvnmon.cmd.NoopApplication;
import dev.mck.mvnmon.nats.PingHealthCheck;
import dev.mck.mvnmon.nats.Subjects;
import io.dropwizard.setup.Environment;
import java.time.Duration;

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
    var nats = configuration.getNats().build(environment);
    environment.jersey().register(new WebhookResource(configuration.getSecret(), nats));
    environment
        .healthChecks()
        .register(
            "backend", new PingHealthCheck(Subjects.BACKEND_PING, Duration.ofSeconds(5), nats));
  }
}
