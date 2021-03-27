package dev.mck.mvnmon.cmd.pom;

import de.pdark.decentxml.Document;
import dev.mck.mvnmon.api.maven.Dependency;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.TableFormatter;
import dev.mck.mvnmon.util.XmlFiles;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.net.URL;
import java.util.Collection;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class DependenciesCommand extends Command {

  public DependenciesCommand() {
    super("dependencies", "Parse and print the dependencies found in a POM file.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser
        .addArgument("url")
        .help("absolute URL of local (file://) or remote (http://) POM file.")
        .type(URL.class);
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
    URL url = (URL) namespace.get("url");
    Document doc = XmlFiles.parse(url);
    Collection<Dependency> dependencies = PomFiles.getDependencies(doc);
    TableFormatter output = new TableFormatter();
    for (Dependency dependency : dependencies) {
      output
          .add("GROUP ID", dependency.getGroupId())
          .add("ARTIFACT ID", dependency.getArtifactId())
          .add("VERSION", dependency.getVersion());
    }
    System.out.println(output.toString());
  }
}
