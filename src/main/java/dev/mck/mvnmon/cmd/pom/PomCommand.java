package dev.mck.mvnmon.cmd.pom;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class PomCommand extends Command {

  // use a sorted map so the subcommands are consistently ordered across executions
  private final SortedMap<String, Command> subcommands;

  public PomCommand() {
    super("pom", "Utilities for POM files.");
    subcommands = new TreeMap<>();
    addSubcommand(new DependenciesCommand());
    addSubcommand(new UpdateDependenciesCommand());
  }

  private void addSubcommand(Command subcommand) {
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
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
    String subcommandName = namespace.getString("subcommand");
    if (subcommandName == null) {
      // this will never happen, as long as configure() is correct
      throw new NullPointerException("no 'subcommand' attribute found in namespace");
    }
    Command subcommand = subcommands.get(subcommandName);
    if (subcommand == null) {
      // this will never happen, as long as configure() is correct
      throw new NullPointerException("unknown subcommand=" + subcommandName);
    }
    subcommand.run(bootstrap, namespace);
  }
}