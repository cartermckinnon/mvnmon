package dev.mck.mvnmon.cmd;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Cli;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Parses and validates the application's configuration.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 */
@Slf4j
public class CheckConfigurationCommand<T extends Configuration> extends ConfiguredCommand<T> {

  private final Class<T> configurationClass;

  public CheckConfigurationCommand(Application<T> application) {
    super("check-configuration", "Parses and validates the configuration file");
    this.configurationClass = application.getConfigurationClass();
  }

  /*
   * Since we don't subclass CheckConfigurationCommand, we need a concrete reference to the configuration
   * class.
   */
  @Override
  protected Class<T> getConfigurationClass() {
    return configurationClass;
  }

  @Override
  protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration)
      throws Exception {
    LOG.info("Configuration is OK");
  }

  /* The stacktrace is redundant as the message contains the yaml error location */
  @Override
  public void onError(Cli cli, Namespace namespace, Throwable e) {
    cli.getStdErr().println(e.getMessage());
  }
}
