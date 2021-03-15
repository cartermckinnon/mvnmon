package dev.mck.mvnmon.cmd.webhook;

import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import dev.mck.mvnmon.cmd.webhook.frontend.FrontendCommand;
import io.dropwizard.Application;
import io.dropwizard.cli.Command;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class WebhookCommand extends EnvironmentCommand<MvnMonConfiguration> {

  // use a sorted map so the subcommands are consistently ordered across executions
  private final SortedMap<String, ExtendedServerCommand<MvnMonConfiguration>> subcommands;

  public WebhookCommand(Application<MvnMonConfiguration> application) {
    super(application, "webhook", "Programs related to GitHub Webhooks.");
    subcommands = new TreeMap<>();
    addSubcommand(new FrontendCommand(application));
  }

  private void addSubcommand(ExtendedServerCommand<MvnMonConfiguration> subcommand) {
    subcommands.put(subcommand.getName(), subcommand);
  }

  @Override
  public void configure(Subparser subparser) {
    for (Command subcommand : subcommands.values()) {
      final Subparser subcommandParser =
          subparser
              .addSubparsers()
              .addParser(subcommand.getName())
              .setDefault("subcommand", subcommand.getName())
              .description(subcommand.getDescription());
      subcommand.configure(subcommandParser);
    }
  }

  @Override
  protected void run(
      Environment environment, Namespace namespace, MvnMonConfiguration configuration)
      throws Exception {
    String subcommandName = namespace.getString("subcommand");
    if (subcommandName == null) {
      // this will never happen, as long as configure() is correct
      throw new NullPointerException("no 'subcommand' attribute found in namespace");
    }
    var subcommand = subcommands.get(subcommandName);
    if (subcommand == null) {
      // this will never happen, as long as configure() is correct
      throw new NullPointerException("unknown subcommand=" + subcommandName);
    }
    subcommand.run(environment, namespace, configuration);
  }
}
