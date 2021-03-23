package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/** Represents the "repository" of a GitHub webhook "push" event. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record Repository(
        @JsonProperty("full_name") String name,
        @JsonProperty("default_branch") String defaultBranch,
        @JsonProperty("id") long id) {
}
