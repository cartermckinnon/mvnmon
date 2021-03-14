package dev.mck.mvnmon.cmd.schedule;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.LifecycleManagedCommand;
import net.sourceforge.argparse4j.inf.Namespace;

public class ScheduleCommand extends LifecycleManagedCommand<MvnMonConfiguration> {

  public ScheduleCommand(Application<MvnMonConfiguration> application) {
    super(application, "schedule", "Queue all Maven artifacts for a version check.");
  }

  @Override
  protected void runManaged(
      Environment environment, Namespace namespace, MvnMonConfiguration configuration)
      throws Exception {
    var jdbi = configuration.buildJdbi(environment);
    var nats = configuration.getNats().build(environment);
    new Scheduler(configuration.getSchedule(), jdbi, nats).run();
  }
}
