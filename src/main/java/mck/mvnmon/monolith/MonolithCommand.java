package mck.mvnmon.monolith;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import mck.mvnmon.MvnMonConfiguration;
import mck.mvnmon.cmd.ExtendedServerCommand;

public class MonolithCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public MonolithCommand(
      Application<MvnMonConfiguration> application, String name, String description) {
    super(application, name, description);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    // TODO Auto-generated method stub

  }
}
