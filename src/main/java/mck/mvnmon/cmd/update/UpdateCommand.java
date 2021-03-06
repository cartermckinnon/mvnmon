package mck.mvnmon.cmd.update;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import java.util.concurrent.TimeUnit;
import mck.mvnmon.MvnMonConfiguration;
import mck.mvnmon.cmd.ExtendedServerCommand;
import mck.mvnmon.ipc.DispatcherManager;
import mck.mvnmon.ipc.Subjects;

public class UpdateCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public UpdateCommand(Application<MvnMonConfiguration> application) {
    super(application, "update", "Persist crawled versions to the database.");
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    var jdbi = configuration.buildJdbi(environment);
    // this must be registered on the environment before the nats connection,
    // so that the nats connection is closed before this executor is shut down
    var executor = environment.lifecycle().scheduledExecutorService("update-batcher-%d").build();
    var nats = configuration.getNats().build(environment);
    var updateBatcher = new UpdateBatcher(jdbi, configuration.getUpdate().getBatchSize());
    executor.scheduleWithFixedDelay(
        updateBatcher,
        configuration.getUpdate().getInterval().toMilliseconds(),
        configuration.getUpdate().getInterval().toMilliseconds(),
        TimeUnit.MILLISECONDS);
    environment
        .lifecycle()
        .manage(new UpdateBatcherManager(updateBatcher, configuration.getUpdate().getInterval()));
    var dispatcher = nats.createDispatcher(new UpdateMessageHandler(updateBatcher));
    dispatcher.subscribe(Subjects.CRAWLED, "update");
    environment.lifecycle().manage(new DispatcherManager(dispatcher));
  }
}
