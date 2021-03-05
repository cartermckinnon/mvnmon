package mck.mvnmon;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import mck.mvnmon.cmd.CrawlCommand;
import mck.mvnmon.cmd.ScheduleCommand;

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
    bootstrap.addCommand(new ScheduleCommand());
    bootstrap.addCommand(new CrawlCommand(this));

    bootstrap.addBundle(
        new MigrationsBundle<MvnMonConfiguration>() {
          @Override
          public DataSourceFactory getDataSourceFactory(MvnMonConfiguration configuration) {
            return configuration.getDb();
          }
        });
  }

  @Override
  public void run(MvnMonConfiguration c, Environment e) throws Exception {}
}
