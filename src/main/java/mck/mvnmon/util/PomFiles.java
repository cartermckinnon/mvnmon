package mck.mvnmon.util;

import de.pdark.decentxml.Attribute;
import de.pdark.decentxml.Document;
import de.pdark.decentxml.Element;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.MavenId;

@Slf4j
public enum PomFiles {
  INSTANCE;

  public static final Pattern MVNMON_IGNORE_COMMENT_PATTERN =
      Pattern.compile("<!--\s+mvnmon:ignore\s+-->");

  public static Collection<MavenId> getDependencies(Document doc) {
    Element rootElement = doc.getRootElement();
    List<Element> dependencies = XmlFiles.findElementsWithName(rootElement, "dependency");
    if (dependencies.isEmpty()) {
      return Collections.emptyList();
    }
    Map<String, String> properties = getProperties(doc);
    Map<String, MavenId> mavenIds = new HashMap<>();
    for (Element element : dependencies) {
      if (MVNMON_IGNORE_COMMENT_PATTERN.matcher(element.toString()).find()) {
        continue;
      }
      String groupId = XmlFiles.firstChildTextContent(element, "groupId");
      String artifactId = XmlFiles.firstChildTextContent(element, "artifactId");
      String groupArtifact = String.join(":", groupId, artifactId);
      if (mavenIds.containsKey(groupArtifact)) {
        continue;
      }
      String version = XmlFiles.firstChildTextContent(element, "version");
      if (version != null) {
        if (version.startsWith("${") && version.endsWith("}")) {
          String versionProperty = version.substring(2, version.length() - 1);
          String propertyVersion = properties.get(versionProperty);
          if (propertyVersion == null) {
            // property may have been marked with mvnmon="ignore"
            // or there's a problem with the POM file, which is (respectfull) not our problem
            continue;
          }
          version = propertyVersion;
        }
        mavenIds.put(groupArtifact, new MavenId(groupId, artifactId, version));
      }
    }
    return mavenIds.values();
  }

  public static Map<String, String> getProperties(Document doc) {
    Map<String, String> properties = new HashMap<>();
    Element rootElement = doc.getRootElement();
    Element propertiesElement = XmlFiles.firstChild(rootElement, "properties");
    if (propertiesElement == null) {
      return Collections.emptyMap();
    }
    ;
    for (Element property : propertiesElement.getChildren()) {
      Attribute ignore = property.getAttribute("mvnmon");
      if (ignore != null && ignore.getValue().equals("ignore")) {
        continue;
      }
      properties.put(property.getName(), property.getText());
    }
    return properties;
  }

  public static boolean updateDependencyVersions(Document doc, Collection<MavenId> mavenIds) {
    if (mavenIds.isEmpty()) {
      return false;
    }
    GroupArtifactLookupTable newVersions = new GroupArtifactLookupTable(mavenIds);
    Map<String, String> propertyChanges = new HashMap<>();
    Element rootElement = doc.getRootElement();
    List<Element> dependencies = XmlFiles.findElementsWithName(rootElement, "dependency");
    boolean update = false;
    for (Element element : dependencies) {
      if (MVNMON_IGNORE_COMMENT_PATTERN.matcher(element.toString()).find()) {
        continue;
      }
      String groupId = XmlFiles.firstChildTextContent(element, "groupId");
      String artifactId = XmlFiles.firstChildTextContent(element, "artifactId");
      Optional<MavenId> newVersion = newVersions.get(groupId, artifactId);
      if (newVersion.isPresent()) {
        String version = XmlFiles.firstChildTextContent(element, "version");
        if (version != null) {
          if (version.startsWith("${") && version.endsWith("}")) {
            String versionProperty = version.substring(2, version.length() - 1);
            propertyChanges.put(versionProperty, newVersion.get().getVersion());
          } else {
            if (XmlFiles.updateFirstChild(element, "version", newVersion.get().getVersion())) {
              update = true;
            }
          }
        }
      }
    }
    return updateProperties(doc, propertyChanges) || update;
  }

  public static boolean updateProperties(Document doc, Map<String, String> propertyChanges) {
    if (propertyChanges.isEmpty()) {
      return false;
    }
    Element rootElement = doc.getRootElement();
    boolean update = false;
    Element properties = XmlFiles.firstChild(rootElement, "properties");
    if (properties == null) {
      throw new IllegalStateException(
          "properties="
              + propertyChanges
              + " need to be updated; but this POM doesn't have a properties block!");
    }
    for (var entry : propertyChanges.entrySet()) {
      String propertyName = entry.getKey();
      String propertyVersion = entry.getValue();
      if (XmlFiles.updateFirstChildIgnoringIfAttribute(
          properties, propertyName, propertyVersion, "mvnmon", "ignore")) {
        update = true;
      }
    }
    return update;
  }

  private static class GroupArtifactLookupTable {
    private final Map<String, MavenId> map;

    private GroupArtifactLookupTable(Collection<MavenId> mavenIds) {
      map = new HashMap<String, MavenId>(mavenIds.size());
      for (var mavenId : mavenIds) {
        String groupArtifact = String.join(":", mavenId.getGroup(), mavenId.getArtifact());
        MavenId conflict = map.putIfAbsent(groupArtifact, mavenId);
        if (conflict != null) {
          throw new IllegalArgumentException("conflict on groupArtifact='" + groupArtifact + "'");
        }
      }
    }

    private Optional<MavenId> get(String group, String artifact) {
      var res = map.get(String.join(":", group, artifact));
      return Optional.ofNullable(res);
    }
  }
}
