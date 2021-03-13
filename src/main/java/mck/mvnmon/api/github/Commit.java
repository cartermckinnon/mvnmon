package mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import mck.mvnmon.util.Strings;
import org.joda.time.DateTime;

/**
 * Represents a commit in a GitHub webhook 'push' event. As of March 10, 2021, this field looked
 * like:
 *
 * <pre>
 * {
 *   "id": "4b73f50417b9da14277eac73a2cb84d455ab3d74",
 *   "tree_id": "166e7885f981ba339131de067b2b0ba1d28a1795",
 *   "distinct": true,
 *   "message": "Create README.md",
 *   "timestamp": "2021-03-10T23:01:27-08:00",
 *   "url": "https://github.com/cartermckinnon/mvnmon/commit/4b73f50417b9da14277eac73a2cb84d455ab3d74",
 *   "author": {
 *     "name": "Carter McKinnon",
 *     "email": "cartermckinnon@gmail.com",
 *     "username": "cartermckinnon"
 *   },
 *   "committer": {
 *     "name": "GitHub",
 *     "email": "noreply@github.com",
 *     "username": "web-flow"
 *   },
 *   "added": [
 *     "README.md"
 *   ],
 *   "removed": [],
 *   "modified": []
 * }
 * </pre>
 */
@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commit {
  private final String id;
  private final List<String> added;
  private final List<String> removed;
  private final List<String> modified;
  private final Author author;
  private final Author committer;
  private final String repositoryName;
  private final DateTime timestamp;

  public Commit(
      @JsonProperty("id") String id,
      @JsonProperty("added") List<String> added,
      @JsonProperty("removed") List<String> removed,
      @JsonProperty("modified") List<String> modified,
      @JsonProperty("author") Author author,
      @JsonProperty("committer") Author committer,
      @JsonProperty("url") URI url,
      @JsonProperty("timestamp") DateTime timestamp) {
    this.id = id;
    this.added = added;
    this.removed = removed;
    this.modified = modified;
    this.author = author;
    this.committer = committer;
    String path = url.getPath();
    int endRepositoryName = Strings.nthIndexOf(path, '/', 3);
    this.repositoryName = path.substring(1, endRepositoryName);
    this.timestamp = timestamp;
  }

  public boolean containsAddedOrModifiedPoms() {
    for (String file : added) {
      if (file.endsWith("pom.xml")) {
        return true;
      }
    }
    for (String file : modified) {
      if (file.endsWith("pom.xml")) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return URLs for the raw content of added and modified POM files in this commit. The URLs look
   *     like: {@code
   *     https://raw.githubusercontent.com/cartermckinnon/mvnmon/4b73f50417b9da14277eac73a2cb84d455ab3d74/README.md}.
   */
  public List<URI> getPomRawUrls() {
    List<URI> rawUrls = new ArrayList<>();
    for (String addedFile : added) {
      if (addedFile.endsWith("pom.xml")) {
        rawUrls.add(rawUrl(addedFile));
      }
    }
    for (String modifiedFile : modified) {
      if (modifiedFile.endsWith("pom.xml")) {
        rawUrls.add(rawUrl(modifiedFile));
      }
    }
    return rawUrls;
  }

  /**
   * @param file in this commit.
   * @return url for raw file content.
   */
  private URI rawUrl(String file) {
    return URI.create(
        String.format("https://raw.githubusercontent.com/%s/%s/%s", repositoryName, id, file));
  }
}
