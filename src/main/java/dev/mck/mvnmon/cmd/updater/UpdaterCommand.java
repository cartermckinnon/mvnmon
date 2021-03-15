package dev.mck.mvnmon.cmd.updater;

import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import dev.mck.mvnmon.nats.DispatcherManager;
import dev.mck.mvnmon.nats.Subjects;
import dev.mck.mvnmon.util.CloseableManager;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class UpdaterCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public UpdaterCommand(Application<MvnMonConfiguration> application) {
    super(application, "updater", "Persist crawled versions to the database.");
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    var jdbi = configuration.buildJdbi(environment);
    // this must be registered on the environment before the nats connection,
    // so that the nats connection is closed before this executor is shut down
    var executor =
        environment.lifecycle().scheduledExecutorService("update-message-handler-%d").build();
    var messageHandler =
        new UpdaterMessageHandler(
            jdbi,
            configuration.getUpdater().getBatchSize(),
            configuration.getUpdater().getInterval(),
            executor);
    environment.lifecycle().manage(new CloseableManager(messageHandler));
    var nats = configuration.getNats().build(environment);
    var dispatcher = nats.createDispatcher(messageHandler);
    dispatcher.subscribe(Subjects.CRAWLED, "update");
    environment.lifecycle().manage(new DispatcherManager(dispatcher));
  }
}
