package mck.mvnmon.pom;

import de.pdark.decentxml.Document;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.net.URL;
import java.util.Collection;
import mck.mvnmon.api.MavenDependency;
import mck.mvnmon.util.PaddedStringBuilder;
import mck.mvnmon.util.PomFiles;
import mck.mvnmon.util.XmlFiles;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class PomDependenciesCommand extends Command {

  public PomDependenciesCommand() {
    super(
        "pom-dependencies",
        "Utility that will parse and print the dependencies found in a POM file.");
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
    Document doc = XmlFiles.parseXmlFile(url);
    Collection<MavenDependency> dependencies = PomFiles.getDependencies(doc);
    int groupLen = 7; // 'GROUPID'
    int artifactLen = 10; // 'ARTIFACTID'
    for (MavenDependency dependency : dependencies) {
      groupLen = Math.max(groupLen, dependency.getGroupId().length());
      artifactLen = Math.max(artifactLen, dependency.getArtifactId().length());
    }
    groupLen += 1;
    artifactLen += 1;
    String header =
        new PaddedStringBuilder()
            .padWith("GROUPID", ' ', groupLen)
            .padWith("ARTIFACTID", ' ', artifactLen)
            .append("VERSION")
            .toString();
    System.out.println(header);
    for (MavenDependency dependency : dependencies) {
      String line =
          new PaddedStringBuilder()
              .padWith(dependency.getGroupId(), ' ', groupLen)
              .padWith(dependency.getArtifactId(), ' ', artifactLen)
              .append(dependency.getVersion())
              .toString();
      System.out.println(line);
    }
  }
}
