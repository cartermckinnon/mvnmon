package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import dev.mck.mvnmon.util.Strings;
import org.joda.time.DateTime;

/** Represents a commit in a GitHub webhook 'push' event. */
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
}
