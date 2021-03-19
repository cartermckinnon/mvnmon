package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import dev.mck.mvnmon.util.Strings;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.joda.time.DateTime;

/** Represents a commit in a GitHub webhook 'push' event. */
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

  public String getId() {
    return id;
  }

  public List<String> getAdded() {
    return added;
  }

  public List<String> getRemoved() {
    return removed;
  }

  public List<String> getModified() {
    return modified;
  }

  public Author getAuthor() {
    return author;
  }

  public Author getCommitter() {
    return committer;
  }

  public String getRepositoryName() {
    return repositoryName;
  }

  public DateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("added", added)
        .add("removed", removed)
        .add("modified", modified)
        .add("author", author)
        .add("committer", committer)
        .add("repositoryName", repositoryName)
        .add("timestamp", timestamp)
        .toString();
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + Objects.hashCode(this.id);
    hash = 67 * hash + Objects.hashCode(this.added);
    hash = 67 * hash + Objects.hashCode(this.removed);
    hash = 67 * hash + Objects.hashCode(this.modified);
    hash = 67 * hash + Objects.hashCode(this.author);
    hash = 67 * hash + Objects.hashCode(this.committer);
    hash = 67 * hash + Objects.hashCode(this.repositoryName);
    hash = 67 * hash + Objects.hashCode(this.timestamp);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Commit other = (Commit) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.repositoryName, other.repositoryName)) {
      return false;
    }
    if (!Objects.equals(this.added, other.added)) {
      return false;
    }
    if (!Objects.equals(this.removed, other.removed)) {
      return false;
    }
    if (!Objects.equals(this.modified, other.modified)) {
      return false;
    }
    if (!Objects.equals(this.author, other.author)) {
      return false;
    }
    if (!Objects.equals(this.committer, other.committer)) {
      return false;
    }
    return Objects.equals(this.timestamp, other.timestamp);
  }
}
