package dev.mck.mvnmon;

import dev.mck.mvnmon.cmd.backend.BackendCommand;
import dev.mck.mvnmon.cmd.frontend.FrontendCommand;
import dev.mck.mvnmon.cmd.pom.PomCommand;
import dev.mck.mvnmon.conf.JdbiConfiguration;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Container for mvnmon commands. This class uses the JdbiConfiguration as its Application type
 * parameter in order to utilize the MigrationsBundle ('db' subcommand).
 */
public class MvnMonApplication extends Application<JdbiConfiguration> {
  public static final void main(String[] args) throws Exception {
    new MvnMonApplication().run(args);
  }

  @Override
  public String getName() {
    return "mvnmon";
  }

  @Override
  public void initialize(Bootstrap<JdbiConfiguration> bootstrap) {
    bootstrap.addCommand(new BackendCommand());
    bootstrap.addCommand(new FrontendCommand());
    bootstrap.addCommand(new PomCommand());
    bootstrap.addBundle(
        new MigrationsBundle<JdbiConfiguration>() {
          @Override
          public DataSourceFactory getDataSourceFactory(JdbiConfiguration configuration) {
            return configuration.getDb();
          }

          @Override
          public String getMigrationsFileName() {
            return "migrations.yaml";
          }
        });
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(),
            // 'false' disables exception when the variable is not defined
            // i.e. it allows default values to be used in the substition brackets
            new EnvironmentVariableSubstitutor(false)));
  }

  @Override
  protected void addDefaultCommands(Bootstrap<JdbiConfiguration> bootstrap) {
    // don't add any of the default commands, this Application is just a container
  }

  @Override
  public void run(JdbiConfiguration c, Environment e) throws Exception {
    // intentionally a no-op; all functionality should be implemented as a command
  }
}
