package mck.mvnmon.cmd;

import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.MvnMonConfiguration;
import mck.mvnmon.sql.MvnMonDao;
import net.sourceforge.argparse4j.inf.Namespace;

@Slf4j
public class ScheduleCommand extends EnvironmentCommand<MvnMonConfiguration> {

  public ScheduleCommand(Application<MvnMonConfiguration> application) {
    super(application, "schedule", "Queue all Maven artifacts for a version check.");
  }

  @Override
  protected void run(Environment e, Namespace n, MvnMonConfiguration c) throws Exception {
    var jdbi = c.buildJdbi(e);

    var dao = jdbi.onDemand(MvnMonDao.class);
  }
}
