package mck.mvnmon.crawl;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import liquibase.pro.packaged.i;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.util.LevenshteinDistance;
import org.apache.maven.artifact.versioning.ComparableVersion;

@Slf4j
public class CrawlUtils {
  public static final String buildUrl(String groupId, String artifactId) {
    return String.format(
        "https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&core=gav&start=0&rows=20",
        groupId, artifactId);
  }

  public static final List<String> parseLatestVersionsFromResponse(byte[] json) {
    return parseLatestVersionsFromResponse(new String(json, StandardCharsets.UTF_8));
  }

  public static final List<String> parseLatestVersionsFromResponse(String json) {
    Object versionsElement = JsonPath.read(json, "$.response.docs[*].v");
    if (versionsElement instanceof List) {
      return (List<String>) versionsElement;
    } else if (versionsElement != null) {
      String msg =
          String.format(
              "unexpected type=%s when parsing versions=%s in response=%s",
              versionsElement.getClass().getName(), versionsElement, json);
      throw new RuntimeException(msg);
    } else {
      String msg = String.format("no latest versions in response={}", json);
      throw new RuntimeException(msg);
    }
  }

  /**
   * Determine what the new version for a MavenArtifact is (if any) from a list of latest versions.
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
        // fall through
    }

    // calculate a score for each candidate that is its edit distance + its position
    // in the list (to penalize earlier upload times).
    // the winning candidate is the one with the lowest score.
    // any tie is broken by insertion order (i.e. upload time).
    int minimumScore = Integer.MAX_VALUE;
    ComparableVersion candidate = null;
    for (int i = 0; i < candidateVersions.size(); i++) {
      ComparableVersion currentCandidate = candidateVersions.get(i);
      int editDistance =
          LevenshteinDistance.getDefaultInstance()
              .apply(currentVersion, currentCandidate.toString());
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
}
