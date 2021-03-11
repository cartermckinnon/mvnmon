package mck.mvnmon.apiserver;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import mck.mvnmon.MvnMonConfiguration;
import mck.mvnmon.command.ExtendedServerCommand;

public class ApiServerCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public ApiServerCommand(Application<MvnMonConfiguration> application) {
    super(application, "api-server", "REST API server for the database.");
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    var jdbi = configuration.buildJdbi(environment);
    environment.jersey().register(new MavenArtifactResource(jdbi));
  }
}
