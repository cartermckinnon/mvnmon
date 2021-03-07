package mck.mvnmon.pom;

import de.pdark.decentxml.Document;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.sink.MavenIdSink;
import mck.mvnmon.util.PomFiles;
import mck.mvnmon.util.XmlFiles;

public class PomFileSink implements MavenIdSink {

  private final File pomFile;
  private final Document doc;
  private final List<MavenId> mavenIds;

  public PomFileSink(File pomFile) {
    if (pomFile.isDirectory()) {
      throw new IllegalArgumentException("directory=" + pomFile + " is not a POM file!");
    }
    if (!pomFile.canWrite()) {
      throw new IllegalArgumentException("pomFile=" + pomFile + " is not writable!");
    }
    this.pomFile = pomFile;
    this.mavenIds = new ArrayList<>();
    // parse the doc up-front in case there's an issue with the file
    try {
      this.doc = XmlFiles.parseXmlFile(pomFile);
    } catch (IOException e) {
      throw new IllegalArgumentException("failed to parse pomFile=" + pomFile, e);
    }
  }

  @Override
  public void close() throws IOException {
    PomFiles.updateDependencyVersions(doc, mavenIds);
    File backupPomFile = new File(pomFile, ".backup");
    File newPomFile = new File(pomFile.getAbsolutePath());
    pomFile.renameTo(backupPomFile);
    try (FileWriter writer = new FileWriter(newPomFile, false)) {
      writer.append(doc.toXML());
    }
  }

  @Override
  public void sink(MavenId mavenId) {
    mavenIds.add(mavenId);
  }
}
