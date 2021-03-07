package mck.mvnmon.pom;

import de.pdark.decentxml.Document;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.net.URL;
import java.util.Collection;
import mck.mvnmon.api.MavenId;
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
    Collection<MavenId> mavenIds = PomFiles.getDependencies(doc);
    int groupLen = 0, artifactLen = 0;
    for (MavenId mavenId : mavenIds) {
      groupLen = Math.max(groupLen, mavenId.getGroup().length());
      artifactLen = Math.max(artifactLen, mavenId.getArtifact().length());
    }
    groupLen += 1;
    artifactLen += 1;
    String header =
        new PaddedStringBuilder()
            .padWith("GROUP", ' ', groupLen)
            .padWith("ARTIFACT", ' ', artifactLen)
            .append("VERSION")
            .toString();
    System.out.println(header);
    for (MavenId mavenId : mavenIds) {
      String line =
          new PaddedStringBuilder()
              .padWith(mavenId.getGroup(), ' ', groupLen)
              .padWith(mavenId.getArtifact(), ' ', artifactLen)
              .append(mavenId.getVersion())
              .toString();
      System.out.println(line);
    }
  }
}
