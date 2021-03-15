package dev.mck.mvnmon.cmd.pom;

import de.pdark.decentxml.Document;
import dev.mck.mvnmon.api.maven.ArtifactUpdate;
import dev.mck.mvnmon.api.maven.Dependency;
import dev.mck.mvnmon.cmd.crawler.CrawlerUtils;
import dev.mck.mvnmon.util.PaddedStringBuilder;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.XmlFiles;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class UpdateDependenciesCommand extends Command {

  public UpdateDependenciesCommand() {
    super(
        "update-dependencies",
        "Update the dependencies found in a POM file to the latest versions.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("pomFilePath").help("path of local POM file.").type(File.class);
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
    File pomFile = (File) namespace.get("pomFilePath");
    Document doc = XmlFiles.parseXmlFile(pomFile);
    Collection<Dependency> dependencies = PomFiles.getDependencies(doc);

    Collection<ArtifactUpdate> updates = new ArrayList<>();
    for (Dependency dependency : dependencies) {
      String crawlUrl = CrawlerUtils.buildUrl(dependency.getGroupId(), dependency.getArtifactId());
      byte[] response = new URL(crawlUrl).openStream().readAllBytes();
      List<String> latestVersions = CrawlerUtils.parseLatestVersionsFromResponse(response);
      PomFiles.getNewVersion(dependency.getVersion(), latestVersions)
          .ifPresent(
              newVersion -> {
                updates.add(
                    new ArtifactUpdate(
                        dependency.getGroupId(),
                        dependency.getArtifactId(),
                        dependency.getVersion(),
                        newVersion));
              });
    }

    if (PomFiles.updateDependencyVersions(doc, updates)) {
      File backupPomFile = new File(pomFile, ".backup");
      File newPomFile = new File(pomFile.getAbsolutePath());
      pomFile.renameTo(backupPomFile);
      try (FileWriter writer = new FileWriter(newPomFile, false)) {
        writer.write(doc.toXML());
      }
    }

    if (updates.isEmpty()) {
      System.out.println("No dependencies updated");
      return;
    }

    if (updates.size() == 1) {
      System.out.println("Updated 1 dependency");
    } else {
      System.out.println("Updated " + updates.size() + " dependencies");
    }

    final String groupHeader = "GROUP ID";
    final String artifactHeader = "ARTIFACT ID";
    final String oldVersionHeader = "OLD VERSION";
    final String newVersionHeader = "NEW VERSION";

    int groupLen = groupHeader.length();
    int artifactLen = artifactHeader.length();
    int oldVersionLen = oldVersionHeader.length();
    for (ArtifactUpdate update : updates) {
      groupLen = Math.max(groupLen, update.getGroupId().length());
      artifactLen = Math.max(artifactLen, update.getArtifactId().length());
      oldVersionLen = Math.max(oldVersionLen, update.getCurrentVersion().length());
    }
    groupLen += 1;
    artifactLen += 1;
    oldVersionLen += 1;
    String header =
        new PaddedStringBuilder()
            .padWith(groupHeader, ' ', groupLen)
            .padWith(artifactHeader, ' ', artifactLen)
            .padWith(oldVersionHeader, ' ', oldVersionLen)
            .append(newVersionHeader)
            .toString();
    System.out.println(header);
    for (ArtifactUpdate update : updates) {
      String line =
          new PaddedStringBuilder()
              .padWith(update.getGroupId(), ' ', groupLen)
              .padWith(update.getArtifactId(), ' ', artifactLen)
              .padWith(update.getCurrentVersion(), ' ', oldVersionLen)
              .append(update.getNewVersion())
              .toString();
      System.out.println(line);
    }
  }
}
