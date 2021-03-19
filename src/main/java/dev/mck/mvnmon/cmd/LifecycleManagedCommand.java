package dev.mck.mvnmon.cmd;

import io.dropwizard.Configuration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command that provides end-of-life lifecycle management for objects registered on an {@link
 * io.dropwizard.setup.Environment}.
 */
public abstract class LifecycleManagedCommand<T extends Configuration>
    extends EnvironmentCommand<T> {

  private static final Logger LOG = LoggerFactory.getLogger(LifecycleManagedCommand.class);

  public LifecycleManagedCommand(String name, String description, Class<T> configurationClass) {
    super(new NoopApplication<>(name, configurationClass), name, description);
  }

  @Override
  protected void run(Environment environment, Namespace namespace, T configuration)
      throws Exception {
    runManaged(environment, namespace, configuration);
    LOG.warn("stopping...");
    environment.lifecycle().getManagedObjects().stream()
        .forEach(
            mo -> {
              try {
                // managed objects must be started before they can be stopped
                if (!mo.isStarted()) {
                  mo.start();
                }
                mo.stop();
              } catch (Exception e) {
                LOG.error("failed to stop {}", mo, e);
              }
            });
  }

  protected abstract void runManaged(Environment environment, Namespace namespace, T configuration)
      throws Exception;
}
