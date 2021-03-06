package mck.mvnmon;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.cmd.CheckConfigurationCommand;
import mck.mvnmon.cmd.apiserver.ApiServerCommand;
import mck.mvnmon.cmd.crawl.CrawlCommand;
import mck.mvnmon.cmd.schedule.ScheduleCommand;
import mck.mvnmon.cmd.update.UpdateCommand;

@Slf4j
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
    // do not add the 'server' command -- we will provide our own,
    // so that Application::run can always be a no-op
    bootstrap.addCommand(new CheckConfigurationCommand<>(this));
  }

  @Override
  public void run(MvnMonConfiguration c, Environment e) throws Exception {
    // intentionally a no-op; all functionality should be implemented as a command
  }
}
