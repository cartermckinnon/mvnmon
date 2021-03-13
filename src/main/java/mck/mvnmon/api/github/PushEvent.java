package mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushEvent {

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private final String ref;
  private final Repository repository;
  private final List<Commit> commits;

  public PushEvent(
      @JsonProperty("ref") String ref,
      @JsonProperty("repository") Repository repository,
      @JsonProperty("commits") List<Commit> commits) {
    this.ref = ref;
    this.repository = repository;
    this.commits = commits;
  }

  /** @return true if this push event was to the repository's default branch; false otherwise. */
  @JsonIgnore
  public boolean isToDefaultBranch() {
    return ref.endsWith(repository.getDefaultBranch());
  }

  @JsonIgnore
  public boolean containsAddedorModifiedPoms() {
    for (Commit commit : commits) {
      if (commit.containsAddedOrModifiedPoms()) {
        return true;
      }
    }
    return false;
  }

  public byte[] toBytes() {
    try {
      return MAPPER.writeValueAsBytes(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("failed to serialize to bytes!", e);
    }
  }
}
