package mck.mvnmon.crawl;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import liquibase.pro.packaged.i;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.util.LevenshteinDistance;
import mck.mvnmon.util.Strings;

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
    LOG.error(
        "attempting to find a newer version for mavenId={} in latestVersions={}",
        mavenId,
        latestVersions);
    if (latestVersions.get(0).equals(mavenId.getVersion())) {
      return Optional.empty();
    }
    List<String> newerVersionsThanCurrent = new ArrayList<>(latestVersions.size());
    for (String version : latestVersions) {
      if (version.equals(mavenId.getVersion())) {
        break;
      }
      newerVersionsThanCurrent.add(version);
    }
    LOG.error("newerVersionsThanCurrent={}", newerVersionsThanCurrent);
    newerVersionsThanCurrent.sort(Strings.createLengthComparator());
    LOG.error("sorted newerVersionsThanCurrent={}", newerVersionsThanCurrent);
    int curDistance;
    SortedMap<Integer, String> scores = new TreeMap<>();
    for (int i = 0; i < newerVersionsThanCurrent.size(); i++) {
      curDistance =
          LevenshteinDistance.getDefaultInstance()
              .apply(mavenId.getVersion(), newerVersionsThanCurrent.get(i));
      if (curDistance > 0) {
        // the older the version, the higher the score
        // the larger the distance, the higher the score
        scores.put(curDistance * i, newerVersionsThanCurrent.get(i));
      } else {
        // we found the current version, no need to look at older ones
        break;
      }
    }
    LOG.error("scores={}", scores);
    int candidateDistance = scores.firstKey();
    String candidate = scores.get(candidateDistance);
    if (candidate.startsWith(mavenId.getVersion())) {
      // if the only change is the addition of a suffix, we have to bail,
      // because the meaning of a suffix can vary too much to make a guess here.
      // for example, org.postgresql:postgresql uses versions of the form "58.0.0.jre7"
      // and a user might currently be on "58.0.0".
      return Optional.empty();
    }
    return Optional.of(candidate);
  }
}
