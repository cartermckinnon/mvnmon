package mck.mvnmon;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import mck.mvnmon.apiserver.ApiServerCommand;
import mck.mvnmon.command.CheckConfigurationCommand;
import mck.mvnmon.crawl.CrawlCommand;
import mck.mvnmon.pom.PomDependenciesCommand;
import mck.mvnmon.pom.PomUpdateDependenciesCommand;
import mck.mvnmon.schedule.ScheduleCommand;
import mck.mvnmon.update.UpdateCommand;

public class MvnMonApplication extends Application<MvnMonConfiguration> {
  public static final void main(String[] args) throws Exception {
    new MvnMonApplication().run(args);
  }

  @Override
  public String getName() {
    return "mvnmon";
  }

  @Override
  public void initialize(Bootstrap<MvnMonConfiguration> bootstrap) {
    bootstrap.addCommand(new ScheduleCommand(this));
    bootstrap.addCommand(new CrawlCommand(this));
    bootstrap.addCommand(new UpdateCommand(this));
    bootstrap.addCommand(new ApiServerCommand(this));
    bootstrap.addCommand(new PomDependenciesCommand());
    bootstrap.addCommand(new PomUpdateDependenciesCommand());
    bootstrap.addBundle(
        new MigrationsBundle<MvnMonConfiguration>() {
          @Override
          public DataSourceFactory getDataSourceFactory(MvnMonConfiguration configuration) {
            return configuration.getDb();
          }
        });
  }

  @Override
  protected void addDefaultCommands(Bootstrap<MvnMonConfiguration> bootstrap) {
    // we do not add the 'server' command -- we will provide our own,
    // so that Application::run can always be a no-op
    bootstrap.addCommand(new CheckConfigurationCommand<>(this));
  }

  @Override
  public void run(MvnMonConfiguration c, Environment e) throws Exception {
    // intentionally a no-op; all functionality should be implemented as a command
  }
}
