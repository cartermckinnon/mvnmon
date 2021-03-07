package mck.mvnmon.pom;

import de.pdark.decentxml.Document;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.source.MavenIdSource;
import mck.mvnmon.util.PomFiles;
import mck.mvnmon.util.XmlFiles;

public class PomSource implements MavenIdSource {

  private final URL pomFileUrl;

  public PomSource(URL pomFileUrl) {
    this.pomFileUrl = pomFileUrl;
  }

  @Override
  public Iterator<MavenId> get() {
    Document doc;
    try {
      doc = XmlFiles.parseXmlFile(pomFileUrl);
    } catch (IOException e) {
      throw new RuntimeException("failed to get parse POM file from url=" + pomFileUrl, e);
    }
    return PomFiles.getDependencies(doc).iterator();
  }
}
