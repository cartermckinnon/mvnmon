package dev.mck.mvnmon.cmd;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * This is a no-op implementation of a Dropwizard Application container; used to enable
 * EnvironmentCommand implementations to use their own Configuration subclasses instead of one
 * central Configuration.
 *
 * <p>Because {@link io.dropwizard.cli.EnvironmentCommand}'s constructor requires an Application
 * instance, which in turn is parameterized by the Configuration type, simply passing the top-level
 * {@link dev.mck.mvnmon.MvnMonApplication} would require that all Commands use the same
 * configuration class. To avoid providing commands with options they don't need (such as access
 * keys), {@link dev.mck.mvnmon.MvnMonApplication} is a simple container for the commands, which
 * define their own configuration types.
 *
 * @param <T> the Configuration implementation.
 */
public class NoopApplication<T extends Configuration> extends Application<T> {

  private final Class<T> configurationClass;
  private final String name;

  public NoopApplication(String name, Class<T> configurationClass) {
    this.name = name;
    this.configurationClass = configurationClass;
  }

  @Override
  public void initialize(Bootstrap<T> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(),
            // 'false' disables exception when the variable is not defined
            // i.e. it allows default values to be used in the substition brackets
            new EnvironmentVariableSubstitutor(false)));
  }

  @Override
  public void run(T configuration, Environment environment) throws Exception {
    // no-op
  }

  @Override
  public Class<T> getConfigurationClass() {
    return this.configurationClass;
  }

  @Override
  protected void addDefaultCommands(Bootstrap<T> bootstrap) {
    bootstrap.addCommand(new CheckConfigurationCommand<>(this));
  }

  @Override
  public String getName() {
    return name;
  }
}
