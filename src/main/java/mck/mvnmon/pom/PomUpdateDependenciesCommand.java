package mck.mvnmon.pom;

import de.pdark.decentxml.Document;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.crawl.CrawlUtils;
import mck.mvnmon.util.PaddedStringBuilder;
import mck.mvnmon.util.PomFiles;
import mck.mvnmon.util.XmlFiles;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class PomUpdateDependenciesCommand extends Command {

  public PomUpdateDependenciesCommand() {
    super(
        "pom-update-dependencies",
        "Utility that will update the dependencies found in a POM file to the latest versions.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("pomFilePath").help("path of local POM file.").type(File.class);
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
    File pomFile = (File) namespace.get("pomFilePath");
    Document doc = XmlFiles.parseXmlFile(pomFile);
    Collection<MavenId> mavenIds = PomFiles.getDependencies(doc);

    Collection<MavenId> newVersions = new ArrayList<>();
    for (MavenId mavenId : mavenIds) {
      String crawlUrl = CrawlUtils.buildUrl(mavenId);
      byte[] response = new URL(crawlUrl).openStream().readAllBytes();
      CrawlUtils.parseNewVersionFromResponse(response, mavenId)
          .ifPresent(
              newVersion -> {
                newVersions.add(mavenId.withNewVersion(newVersion));
              });
    }

    if (PomFiles.updateDependencyVersions(doc, newVersions)) {
      File backupPomFile = new File(pomFile, ".backup");
      File newPomFile = new File(pomFile.getAbsolutePath());
      pomFile.renameTo(backupPomFile);
      try (FileWriter writer = new FileWriter(newPomFile, false)) {
        writer.write(doc.toXML());
      }
    }

    if (newVersions.isEmpty()) {
      System.out.println("No dependencies updated");
      return;
    }

    if (newVersions.size() == 1) {
      System.out.println("Updated 1 dependency");
    } else {
      System.out.println("Updated " + newVersions.size() + " dependencies");
    }

    int groupLen = 0, artifactLen = 0;
    for (MavenId mavenId : newVersions) {
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
    for (MavenId mavenId : newVersions) {
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
