package dev.mck.mvnmon.cmd.pom;

import de.pdark.decentxml.Document;
import dev.mck.mvnmon.api.maven.ArtifactUpdate;
import dev.mck.mvnmon.api.maven.Dependency;
import dev.mck.mvnmon.cmd.backend.crawler.CrawlerUtils;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.TableFormatter;
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
    Document doc = XmlFiles.parse(pomFile);
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
                        newVersion.getLeft()));
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

    TableFormatter output = new TableFormatter();
    for (ArtifactUpdate update : updates) {
      output
          .add("GROUP ID", update.getGroupId())
          .add("ARTIFACT ID", update.getArtifactId())
          .add("OLD VERSION", update.getCurrentVersion())
          .add("NEW VERSION", update.getNewVersion());
    }
    System.out.println(output.toString());
  }
}
