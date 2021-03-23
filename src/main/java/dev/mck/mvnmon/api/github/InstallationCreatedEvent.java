package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record InstallationCreatedEvent(
        @JsonProperty("repositories") List<Repository> repositories,
        @JsonProperty("installation") Installation installation) {}
