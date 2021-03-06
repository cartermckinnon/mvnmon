package mck.mvnmon.cmd;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

/**
 * A command that provides end-of-life lifecycle management for objects registered on an {@link
 * io.dropwizard.setup.Environment}.
 */
@Slf4j
public abstract class LifecycleManagedCommand<T extends Configuration>
    extends EnvironmentCommand<T> {

  private final ContainerLifeCycle containerLifeCycle;

  public LifecycleManagedCommand(Application<T> application, String name, String description) {
    super(application, name, description);
    containerLifeCycle = new ContainerLifeCycle();
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
