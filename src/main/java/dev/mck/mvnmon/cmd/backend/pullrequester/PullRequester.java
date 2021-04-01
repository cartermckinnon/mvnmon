package dev.mck.mvnmon.cmd.backend.pullrequester;

import de.pdark.decentxml.Document;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.api.maven.ArtifactUpdate;
import dev.mck.mvnmon.api.maven.Pom;
import dev.mck.mvnmon.sql.RepositoryDao;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.XmlFiles;
import java.util.Collections;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullRequester implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(PullRequester.class);

  private final Pom pom;
  private final ArtifactConsumer consumer;
  private final String newVersion;
  private final Jdbi jdbi;
  private final List<String> versionCandidates;

  public PullRequester(
      Jdbi jdbi,
      Pom pom,
      ArtifactConsumer consumer,
      String newVersion,
      List<String> versionCandidates) {
    this.jdbi = jdbi;
    this.pom = pom;
    this.consumer = consumer;
    this.newVersion = newVersion;
    this.versionCandidates = versionCandidates;
  }

  @Override
  public void run() {
    try {
      throwingRun();
    } catch (Exception e) {
      LOG.error("failed to open pull request for consumer={}", consumer, e);
    }
  }

  public void throwingRun() throws Exception {
    RepositoryDao repositoryDao = jdbi.onDemand(RepositoryDao.class);
    GitHub github = GitHub.connectUsingOAuth(repositoryDao.getToken(pom.getRepositoryId()));
    GHRepository repository = github.getRepositoryById(pom.getRepositoryId());
    GHBranch defaultBranch = repository.getBranch(repository.getDefaultBranch());
    GHCommit headCommit = repository.getCommit(defaultBranch.getSHA1());
    GHContent existingPom = repository.getFileContent(pom.getPath(), repository.getDefaultBranch());
    Document doc = XmlFiles.parse(existingPom.read());
    PomFiles.updateDependencyVersions(
        doc,
        Collections.singleton(
            new ArtifactUpdate(
                consumer.getGroupId(),
                consumer.getArtifactId(),
                consumer.getCurrentVersion(),
                newVersion)));
    String updatedPom = doc.toXML();
    GHTree newTree =
        repository
            .createTree()
            .baseTree(headCommit.getTree().getSha())
            .add(pom.getPath(), updatedPom, false) // 'false' -> not executable
            .create();
    String title = title(consumer, newVersion);
    GHCommit commit =
        repository
            .createCommit()
            .parent(headCommit.getSHA1())
            .tree(newTree.getSha())
            .message(title)
            .create();
    String branch = branch(consumer);
    repository.createRef("refs/heads/" + branch, commit.getSHA1());
    repository.createPullRequest(
        title,
        branch,
        repository.getDefaultBranch(),
        body(consumer, newVersion, versionCandidates));
    LOG.info("created pull request for consumer={} newVersion={}", consumer, newVersion);
  }

  protected static final String title(ArtifactConsumer consumer, String newVersion) {
    return String.format(
        "Update %s:%s from %s to %s",
        consumer.getGroupId(), consumer.getArtifactId(), consumer.getCurrentVersion(), newVersion);
  }

  protected static final String branch(ArtifactConsumer consumer) {
    return new StringBuilder("mvnmon/")
        .append(consumer.getGroupId())
        .append('-')
        .append(consumer.getArtifactId())
        .append('-')
        .append(consumer.getPomId()) // so we don't encounter branch name conflicts
        .toString();
  }

  private static final String BODY_FORMAT =
      """
      I determined that `%s:%s` could be updated from `%s` to `%s`.

      :warning: **Please ensure that this change does not break your build before merging!** :warning:
      """;

  protected static final String body(
      ArtifactConsumer consumer, String newVersion, List<String> candidateVersions) {
    String preamble =
        String.format(
            BODY_FORMAT,
            consumer.getGroupId(),
            consumer.getArtifactId(),
            consumer.getCurrentVersion(),
            newVersion);
    return preamble + otherOptions(newVersion, candidateVersions);
  }

  protected static final String otherOptions(String selectedVersion, List<String> versions) {
    if (versions.isEmpty()) {
      return "";
    }
    if (versions.size() == 1 && versions.get(0).equals(selectedVersion)) {
      return "";
    }
    StringBuilder s = new StringBuilder("\nI don't always get it right; ");
    if (versions.size() == 1) {
      s.append("here's another option");
    } else {
      s.append("here are some other options");
    }
    s.append(":");
    for (String version : versions) {
      s.append("\n- `").append(version).append('`');
      if (version.equals(selectedVersion)) {
        s.append(" *(selected)*");
      }
    }
    return s.toString();
  }
}
