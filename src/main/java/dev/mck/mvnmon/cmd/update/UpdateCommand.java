package dev.mck.mvnmon.cmd.update;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import dev.mck.mvnmon.nats.DispatcherManager;
import dev.mck.mvnmon.nats.Subjects;
import dev.mck.mvnmon.util.CloseableManager;

public class UpdateCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public UpdateCommand(Application<MvnMonConfiguration> application) {
    super(application, "update", "Persist crawled versions to the database.");
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    var jdbi = configuration.buildJdbi(environment);
    // this must be registered on the environment before the nats connection,
    // so that the nats connection is closed before this executor is shut down
    var executor =
        environment.lifecycle().scheduledExecutorService("update-message-handler-%d").build();
    var messageHandler =
        new UpdateMessageHandler(
            jdbi,
            configuration.getUpdate().getBatchSize(),
            configuration.getUpdate().getInterval(),
            executor);
    environment.lifecycle().manage(new CloseableManager(messageHandler));
    var nats = configuration.getNats().build(environment);
    var dispatcher = nats.createDispatcher(messageHandler);
    dispatcher.subscribe(Subjects.CRAWLED, "update");
    environment.lifecycle().manage(new DispatcherManager(dispatcher));
  }
}
