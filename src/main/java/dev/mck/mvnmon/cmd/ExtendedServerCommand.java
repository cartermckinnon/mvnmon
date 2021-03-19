package dev.mck.mvnmon.cmd;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the Dropwizard 'server' command to allow multiple such commands per Application.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 */
public abstract class ExtendedServerCommand<T extends Configuration> extends EnvironmentCommand<T> {

  private static final Logger LOG = LoggerFactory.getLogger(ExtendedServerCommand.class);

  private final Class<T> configurationClass;

  /**
   * A constructor to allow reuse of the server command as a different name
   *
   * @param application the application using this command
   * @param name the argument name to invoke this command
   * @param description a summary of what the command does
   */
  public ExtendedServerCommand(Application<T> application, String name, String description) {
    super(application, name, description);
    this.configurationClass = application.getConfigurationClass();
  }

  /*
   * Since we don't subclass ServerCommand, we need a concrete reference to the configuration
   * class.
   */
  @Override
  protected Class<T> getConfigurationClass() {
    return configurationClass;
  }

  @Override
  public void run(Environment environment, Namespace namespace, T configuration) throws Exception {
    run(environment, configuration);
    final Server server = configuration.getServerFactory().build(environment);
    try {
      server.addLifeCycleListener(new LifeCycleListener());
      cleanupAsynchronously();
      server.start();
    } catch (Exception e) {
      LOG.error("Unable to start server, shutting down", e);
      try {
        server.stop();
      } catch (Exception e1) {
        LOG.warn("Failure during stop server", e1);
      }
      try {
        cleanup();
      } catch (Exception e2) {
        LOG.warn("Failure during cleanup", e2);
      }
      throw e;
    }
  }

  protected abstract void run(Environment environment, T configuration) throws Exception;

  private class LifeCycleListener extends AbstractLifeCycle.AbstractLifeCycleListener {
    @Override
    public void lifeCycleStopped(LifeCycle event) {
      cleanup();
    }
  }
}
