package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents an "author" or "committer" of a GitHub webhook "push" event. */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record Author(
    @JsonProperty("name") String name,
    @JsonProperty("email") String email,
    @JsonProperty("username") String username) {}
