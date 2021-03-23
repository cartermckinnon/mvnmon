package dev.mck.mvnmon.api.github;

import static dev.mck.mvnmon.util.Serialization.MAPPER;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class SerializationTests {

  @Test
  public void account() throws Exception {
    Account account = new Account("login");

    String expected =
        MAPPER.writeValueAsString(
            MAPPER.readValue(fixture("fixtures/account.json"), Account.class));

    assertThat(MAPPER.writeValueAsString(account)).isEqualTo(expected);
  }

  @Test
  public void author() throws Exception {
    Author author = new Author("name", "email", "username");

    String expected =
        MAPPER.writeValueAsString(MAPPER.readValue(fixture("fixtures/author.json"), Author.class));

    assertThat(MAPPER.writeValueAsString(author)).isEqualTo(expected);
  }

  @Test
  public void installation() throws Exception {
    var account = new Account("login");
    var installation = new Installation(123, account);

    String expected =
        MAPPER.writeValueAsString(
            MAPPER.readValue(fixture("fixtures/installation.json"), Installation.class));

    assertThat(MAPPER.writeValueAsString(installation)).isEqualTo(expected);
  }

  @Test
  public void repository() throws Exception {
    var repository = new Repository("foo/bar", "main", 123);

    String expected =
        MAPPER.writeValueAsString(
            MAPPER.readValue(fixture("fixtures/repository.json"), Repository.class));

    assertThat(MAPPER.writeValueAsString(repository)).isEqualTo(expected);
  }

  @Test
  public void installationCreatedEvent() throws Exception {
    // default branch is null because this event's doesn't have it (only pushes do)
    var repository = new Repository("foo/bar", null, 123);
    var account = new Account("foo");
    var installation = new Installation(123, account);
    var event = new InstallationCreatedEvent(Collections.singletonList(repository), installation);

    String expected =
        MAPPER.writeValueAsString(
            MAPPER.readValue(
                fixture("fixtures/installation-created-event.json"),
                InstallationCreatedEvent.class));

    assertThat(MAPPER.writeValueAsString(event)).isEqualTo(expected);
  }

  @Test
  public void installationDeletedEvent() throws Exception {
    var account = new Account("Codertocat");
    var installation = new Installation(5, account);
    var event = new InstallationDeletedEvent(installation);

    String expected =
        MAPPER.writeValueAsString(
            MAPPER.readValue(
                fixture("fixtures/installation-deleted-event.json"),
                InstallationDeletedEvent.class));

    assertThat(MAPPER.writeValueAsString(event)).isEqualTo(expected);
  }

  @Test
  public void installationRepositoriesAddedEvent() throws Exception {
    var repository = new Repository("Codertocat/Space", null, 119);
    var account = new Account("Codertocat");
    var installation = new Installation(5, account);
    var event =
        new InstallationRepositoriesAddedEvent(Collections.singletonList(repository), installation);

    String expected =
        MAPPER.writeValueAsString(
            MAPPER.readValue(
                fixture("fixtures/installation-repositories-added-event.json"),
                InstallationRepositoriesAddedEvent.class));

    assertThat(MAPPER.writeValueAsString(event)).isEqualTo(expected);
  }

  @Test
  public void pushEvent() throws Exception {
    var repository = new Repository("mvnmon/test", "main", 123);
    var author = new Author("mvnmon", "80447704+mvnmon@users.noreply.github.com", "mvnmon");
    var committer = new Author("GitHub", "noreply@github.com", "web-flow");
    var url =
        new URI("https://github.com/mvnmon/test/commit/76ff14424d89c1a788ead25ce20c0fbbff1b82bc");
    var timestamp = new DateTime("2021-03-13T19:52:25-08:00");
    var commit =
        new Commit(
            "76ff14424d89c1a788ead25ce20c0fbbff1b82bc",
            Collections.singletonList("added/pom.xml"),
            Collections.singletonList("removed/pom.xml"),
            Collections.singletonList("modified/pom.xml"),
            author,
            committer,
            url,
            timestamp);
    var event = new PushEvent("refs/heads/main", repository, Collections.singletonList(commit));

    String expected =
        MAPPER.writeValueAsString(
            MAPPER.readValue(fixture("fixtures/push-event.json"), PushEvent.class));

    assertThat(MAPPER.writeValueAsString(event)).isEqualTo(expected);
  }
}
