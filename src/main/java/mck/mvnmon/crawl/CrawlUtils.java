package mck.mvnmon.crawl;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import liquibase.pro.packaged.i;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.util.LevenshteinDistance;
import org.apache.maven.artifact.versioning.ComparableVersion;

@Slf4j
public class CrawlUtils {
  public static final String buildUrl(MavenId mavenId) {
    return String.format(
        "https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&core=gav&start=0&rows=5",
        mavenId.getGroup(), mavenId.getArtifact());
  }

  public static final Optional<String> parseNewVersionFromResponse(byte[] json, MavenId mavenId) {
    return parseNewVersionFromResponse(new String(json, StandardCharsets.UTF_8), mavenId);
  }

  public static final Optional<String> parseNewVersionFromResponse(String json, MavenId mavenId) {
    Object versionsElement = JsonPath.read(json, "$.response.docs[*].v");
    if (versionsElement instanceof List) {
      List<String> versions = (List<String>) versionsElement;
      return getNewVersion(mavenId, versions);
    } else if (versionsElement != null) {
      String msg =
          String.format(
              "unexpected type=%s when parsing version=%s for mavenId=%s in response=%s",
              versionsElement.getClass().getName(), versionsElement, mavenId, json);
      throw new RuntimeException(msg);
    } else {
      String msg = String.format("no version for mavenId={} in response={}", mavenId, json);
      throw new RuntimeException(msg);
    }
  }

  /**
   * Determine what the new version for a MavenId is (if any) from a list of crawled latest
   * versions.
   *
   * @param mavenId
   * @param latestVersions crawled latest versions; assumed to be in descending order of upload
   *     timestamp.
   * @return new version, if one was determined.
   */
  public static final Optional<String> getNewVersion(MavenId mavenId, List<String> latestVersions) {
    if (latestVersions.isEmpty()) {
      return Optional.empty();
    }

    if (latestVersions.get(0).equals(mavenId.getVersion())) {
      // the current version is the latest version
      return Optional.empty();
    }

    // use maven's version comparison to filter the candidates to those equal or newer
    // we (potentially) leave the current version in the mix, to prevent undesirable
    // qualifier switches, such as from org.postgresql:postgresql 42.2.19 to 42.2.19.jre7
    ComparableVersion currentVersion = new ComparableVersion(mavenId.getVersion());
    List<ComparableVersion> newerVersionsThanCurrent =
        latestVersions.stream()
            .map(ComparableVersion::new)
            .filter(v -> currentVersion.compareTo(v) <= 0)
            .collect(Collectors.toList());

    switch (newerVersionsThanCurrent.size()) {
      case 0:
        // this case should be covered by the equality check early-on,
        // but is possible (if artifacts are deleted, for example)
        // we don't do downgrades!
        return Optional.empty();
      case 1:
        return Optional.of(newerVersionsThanCurrent.get(0).toString());
    }

    // calculate a score for each candidate that is its edit distance + its position
    // in the list (to penalize earlier upload times)
    SortedMap<Integer, ComparableVersion> distances = new TreeMap<>();
    for (int i = 0; i < newerVersionsThanCurrent.size(); i++) {
      ComparableVersion version = newerVersionsThanCurrent.get(i);
      int distance =
          LevenshteinDistance.getDefaultInstance().apply(mavenId.getVersion(), version.toString());
      distances.put(distance + i, version);
    }

    ComparableVersion candidate = distances.get(distances.firstKey());
    // we potentially left the current version in the mix, so need to check for it here.
    if (currentVersion.equals(candidate)) {
      return Optional.empty();
    }
    return Optional.of(candidate.toString());
  }
}
