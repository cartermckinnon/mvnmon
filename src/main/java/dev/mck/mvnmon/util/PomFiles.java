package dev.mck.mvnmon.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import de.pdark.decentxml.Attribute;
import de.pdark.decentxml.Document;
import de.pdark.decentxml.Element;
import dev.mck.mvnmon.api.maven.ArtifactUpdate;
import dev.mck.mvnmon.api.maven.Dependency;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum PomFiles {
  INSTANCE;

  private static final Logger LOG = LoggerFactory.getLogger(PomFiles.class);

  public static final Pattern MVNMON_IGNORE_COMMENT_PATTERN =
      Pattern.compile("<!-- +mvnmon:ignore +-->");

  public static Collection<Dependency> getDependencies(Document doc) {
    Element rootElement = doc.getRootElement();
    List<Element> dependencies = XmlFiles.findElementsWithName(rootElement, "dependency");
    if (dependencies.isEmpty()) {
      return Collections.emptyList();
    }
    Map<String, String> properties = getProperties(doc);
    Map<String, Dependency> mavenDependencies = new HashMap<>();
    for (Element dependency : dependencies) {
      if (MVNMON_IGNORE_COMMENT_PATTERN.matcher(dependency.toString()).find()) {
        continue;
      }
      String groupId = XmlFiles.firstChildTextContent(dependency, "groupId");
      String artifactId = XmlFiles.firstChildTextContent(dependency, "artifactId");
      String groupArtifact = String.join(":", groupId, artifactId);
      if (mavenDependencies.containsKey(groupArtifact)) {
        continue;
      }
      String version = XmlFiles.firstChildTextContent(dependency, "version");
      if (version != null) {
        if (version.startsWith("${") && version.endsWith("}")) {
          String versionProperty = version.substring(2, version.length() - 1);
          String propertyVersion = properties.get(versionProperty);
          if (propertyVersion == null) {
            // property may have been marked with mvnmon="ignore"
            // or there's a problem with the POM file, which is (respectfully) not our problem
            continue;
          }
          version = propertyVersion;
        }
        mavenDependencies.put(groupArtifact, new Dependency(groupId, artifactId, version));
      }
    }
    return mavenDependencies.values();
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

  public static boolean updateDependencyVersions(
      Document doc, Collection<ArtifactUpdate> artifactUpdates) {
    if (artifactUpdates.isEmpty()) {
      return false;
    }
    GroupArtifactLookupTable updateTable = new GroupArtifactLookupTable(artifactUpdates);
    Map<String, String> propertyChanges = new HashMap<>();
    Element rootElement = doc.getRootElement();
    List<Element> dependencies = XmlFiles.findElementsWithName(rootElement, "dependency");
    boolean update = false;
    for (Element dependency : dependencies) {
      if (MVNMON_IGNORE_COMMENT_PATTERN.matcher(dependency.toString()).find()) {
        continue;
      }
      String groupId = XmlFiles.firstChildTextContent(dependency, "groupId");
      String artifactId = XmlFiles.firstChildTextContent(dependency, "artifactId");
      Optional<ArtifactUpdate> artifactUpdate = updateTable.get(groupId, artifactId);
      if (artifactUpdate.isPresent()) {
        String version = XmlFiles.firstChildTextContent(dependency, "version");
        if (version != null) {
          if (version.startsWith("${") && version.endsWith("}")) {
            String versionProperty = version.substring(2, version.length() - 1);
            propertyChanges.put(versionProperty, artifactUpdate.get().getNewVersion());
          } else {
            if (XmlFiles.updateFirstChild(
                dependency, "version", artifactUpdate.get().getNewVersion())) {
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
              + " need to be updated; but this POM doesn\'t have a properties block!");
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

  /**
   * Determine what the new version for an artifact is (if any) from a list of latest versions.
   *
   * @param currentVersion
   * @param latestVersions crawled latest versions; assumed to be in descending order of upload
   *     timestamp.
   * @return new version, if one was determined.
   */
  public static final Optional<String> getNewVersion(
      String currentVersion, List<String> latestVersions) {
    if (latestVersions.isEmpty()) {
      return Optional.empty();
    }
    if (latestVersions.get(0).equals(currentVersion)) {
      // the current version is the latest version
      return Optional.empty();
    }
    // use maven's version comparison to filter the candidates to those equal or newer.
    // we (potentially) leave the current version in the mix, to prevent undesirable
    // qualifier switches, such as from org.postgresql:postgresql 42.2.19 to 42.2.19.jre7
    ComparableVersion comparableCurrentVersion = new ComparableVersion(currentVersion);
    List<ComparableVersion> candidateVersions =
        latestVersions.stream()
            .map(ComparableVersion::new)
            .filter(v -> comparableCurrentVersion.compareTo(v) <= 0)
            .collect(Collectors.toList());
    switch (candidateVersions.size()) {
      case 0:
        // this case should be covered by the equality check early-on in most cases,
        // but is possible (if artifacts are deleted, for example)
        // we don't do downgrades!
        return Optional.empty();
      case 1:
        return Optional.of(candidateVersions.get(0).toString());
      default:
    }
    // fall through
    // calculate a score for each candidate that is its edit distance + its position
    // in the list (to penalize earlier upload times).
    // the winning candidate is the one with the lowest score.
    // any tie is broken by insertion order (i.e. upload time).
    int minimumScore = Integer.MAX_VALUE;
    ComparableVersion candidate = null;
    for (int i = 0; i < candidateVersions.size(); i++) {
      ComparableVersion currentCandidate = candidateVersions.get(i);
      int editDistance = LevenshteinDistance.apply(currentVersion, currentCandidate.toString());
      int score = editDistance + i;
      if (score < minimumScore) {
        minimumScore = score;
        candidate = currentCandidate;
      }
    }
    if (candidate == null) {
      // something has gone horribly wrong
      throw new IllegalStateException(
          "no candidate was chosen during the scoring process; this should never happen!");
    }
    // we potentially left the current version in the mix, so need to check for it here.
    if (comparableCurrentVersion.equals(candidate)) {
      return Optional.empty();
    }
    return Optional.of(candidate.toString());
  }

  public static long hashDependencies(Collection<Dependency> dependencies) {
    var sortedNames =
        dependencies.stream().map(d -> d.getGroupId() + d.getArtifactId()).sorted().toList();
    Hasher hash = Hashing.farmHashFingerprint64().newHasher();
    for (String name : sortedNames) {
      hash.putString(name, UTF_8);
    }
    return hash.hash().asLong();
  }

  private static class GroupArtifactLookupTable {
    private final Map<String, ArtifactUpdate> map;

    private GroupArtifactLookupTable(Collection<ArtifactUpdate> artifactUpdates) {
      map = new HashMap<String, ArtifactUpdate>(artifactUpdates.size());
      for (var artifactUpdate : artifactUpdates) {
        String groupArtifact =
            String.join(":", artifactUpdate.getGroupId(), artifactUpdate.getArtifactId());
        ArtifactUpdate conflict = map.putIfAbsent(groupArtifact, artifactUpdate);
        if (conflict != null) {
          throw new IllegalArgumentException("conflict on groupArtifact=\'" + groupArtifact + "\'");
        }
      }
    }

    private Optional<ArtifactUpdate> get(String group, String artifact) {
      var res = map.get(String.join(":", group, artifact));
      return Optional.ofNullable(res);
    }
  }
}
