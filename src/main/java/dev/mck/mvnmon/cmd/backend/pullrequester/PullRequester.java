package dev.mck.mvnmon.cmd.backend.pullrequester;

import de.pdark.decentxml.Document;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.api.maven.ArtifactUpdate;
import dev.mck.mvnmon.api.maven.Pom;
import dev.mck.mvnmon.sql.RepositoryDao;
import dev.mck.mvnmon.util.Pair;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.XmlFiles;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import org.jdbi.v3.core.Jdbi;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullRequester implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(PullRequester.class);

  private final Pom pom;
  private final ArtifactConsumer consumer;
  private final String newVersion;
  private final Jdbi jdbi;

  public PullRequester(Jdbi jdbi, Pom pom, ArtifactConsumer consumer, String newVersion) {
    this.jdbi = jdbi;
    this.pom = pom;
    this.consumer = consumer;
    this.newVersion = newVersion;
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
    GHCommit head = repository.getCommit(defaultBranch.getSHA1());
    Optional<Pair<String, GHTreeEntry>> tree = findPomTreeEntry(repository, head.getTree());
    if (tree.isPresent()) {
      byte[] bytes = tree.get().getRight().readAsBlob().readAllBytes();
      String pom = new String(bytes, StandardCharsets.UTF_8);
      Document doc = XmlFiles.parse(pom);
      PomFiles.updateDependencyVersions(
          doc,
          Collections.singleton(
              new ArtifactUpdate(
                  consumer.getGroupId(),
                  consumer.getArtifactId(),
                  consumer.getCurrentVersion(),
                  newVersion)));
      String updatedPom = doc.toXML();
      GHTree updatedTree = // 'false' -> not executable
          repository
              .createTree()
              .baseTree(tree.get().getLeft())
              .add("pom.xml", updatedPom, false)
              .create();
      String title = title(consumer, newVersion);
      GHCommit commit =
          repository
              .createCommit()
              .parent(head.getSHA1())
              .tree(updatedTree.getSha())
              .message(title)
              .create();
      String branch = branch(consumer);
      repository.createRef("refs/heads/" + branch, commit.getSHA1());
      repository.createPullRequest(
          title, branch, repository.getDefaultBranch(), body(consumer, newVersion));
      LOG.info("created pull request for consumer={} for version={}", consumer, newVersion);
    }
  }

  private Optional<Pair<String, GHTreeEntry>> findPomTreeEntry(
      GHRepository repository, GHTree root) {
    String[] parts = pom.getPath().split("/");
    GHTree tree = root;
    String parentSha = root.getSha();
    GHTreeEntry entry = null;
    for (String part : parts) {
      entry = tree.getEntry(part);
      if (entry == null) {
        return Optional.empty();
      }
      if (entry.getPath().endsWith(part)) {
        break;
      }
      parentSha = entry.getSha();
      try {
        tree = repository.getTree(entry.getSha());
      } catch (IOException e) {
        throw new RuntimeException("failed to walk tree for consumer=" + consumer, e);
      }
    }
    if (entry.getType().equals("blob")) {
      return Optional.of(new Pair<>(parentSha, entry));
    }
    return Optional.empty();
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
        .toString();
  }

  protected static final String body(ArtifactConsumer consumer, String newVersion) {
    return String.format(
        "I determined that `%s:%s` could be updated from `%s` to `%s`.\n\n:warning: **Please ensure that this change does not break your build before merging!** :warning:",
        consumer.getGroupId(), consumer.getArtifactId(), consumer.getCurrentVersion(), newVersion);
  }
}
