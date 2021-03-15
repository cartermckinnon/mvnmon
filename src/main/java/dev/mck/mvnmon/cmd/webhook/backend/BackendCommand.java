package dev.mck.mvnmon.cmd.webhook.backend;

import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import dev.mck.mvnmon.nats.DispatcherManager;
import dev.mck.mvnmon.nats.Subjects;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class BackendCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public BackendCommand(Application<MvnMonConfiguration> application) {
    super(application, "backend", "Daemon to handle GitHub webhook events.");
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    var jdbi = configuration.buildJdbi(environment);
    var nats = configuration.getNats().build(environment);

    var push =
        nats.createDispatcher(new PushEventHandler(jdbi))
            .subscribe(Subjects.hook("push"), "push-handler");
    environment.lifecycle().manage(new DispatcherManager(push));
  }
}
