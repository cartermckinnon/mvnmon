package dev.mck.mvnmon.api.maven;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class Pom {
  public final long repositoryId;
  public final String path;
  public final long dependencyHash;

  public Pom(long repositoryId, String path, long dependencyHash) {
    this.repositoryId = repositoryId;
    this.path = path;
    this.dependencyHash = dependencyHash;
  }

  public long getRepositoryId() {
    return this.repositoryId;
  }

  public String getPath() {
    return this.path;
  }

  public long getDependencyHash() {
    return this.dependencyHash;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("repositoryId", repositoryId)
        .add("path", path)
        .add("dependencyHash", dependencyHash)
        .toString();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (int) (this.repositoryId ^ (this.repositoryId >>> 32));
    hash = 67 * hash + Objects.hashCode(this.path);
    hash = 67 * hash + (int) (this.dependencyHash ^ (this.dependencyHash >>> 32));
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
    final Pom other = (Pom) obj;
    if (this.repositoryId != other.repositoryId) {
      return false;
    }
    if (this.dependencyHash != other.dependencyHash) {
      return false;
    }
    return Objects.equals(this.path, other.path);
  }
}
