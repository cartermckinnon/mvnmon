package dev.mck.mvnmon.cmd.crawler;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrawlerUtils {
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
}
