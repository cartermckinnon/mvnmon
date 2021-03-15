package dev.mck.mvnmon.cmd.scheduler;

import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.LifecycleManagedCommand;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;

public class SchedulerCommand extends LifecycleManagedCommand<MvnMonConfiguration> {

  public SchedulerCommand(Application<MvnMonConfiguration> application) {
    super(application, "scheduler", "Queue all Maven artifacts for a version check.");
  }

  @Override
  protected void runManaged(
      Environment environment, Namespace namespace, MvnMonConfiguration configuration)
      throws Exception {
    var jdbi = configuration.buildJdbi(environment);
    var nats = configuration.getNats().build(environment);
    new Scheduler(configuration.getScheduler(), jdbi, nats).run();
  }
}
