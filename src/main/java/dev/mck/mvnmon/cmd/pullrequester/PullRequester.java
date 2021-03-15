package dev.mck.mvnmon.cmd.pullrequester;

import de.pdark.decentxml.Document;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.api.maven.ArtifactUpdate;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.XmlFiles;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;

@Slf4j
public class PullRequester implements Runnable {
  private final ArtifactConsumer consumer;
  private final String newVersion;
  private final GitHub github;

  public PullRequester(GitHub github, ArtifactConsumer consumer, String newVersion) {
    this.consumer = consumer;
    this.newVersion = newVersion;
    this.github = github;
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
    GHRepository repository = github.getRepository(consumer.getRepository());
    GHBranch defaultBranch = repository.getBranch(repository.getDefaultBranch());
    GHCommit head = repository.getCommit(defaultBranch.getSHA1());
    Optional<GHTreeEntry> tree = findPomTreeEntry(repository, head.getTree());
    if (tree.isPresent()) {
      byte[] bytes = tree.get().readAsBlob().readAllBytes();
      String pom = new String(bytes, StandardCharsets.UTF_8);
      Document doc = XmlFiles.parseXmlFile(pom);
      PomFiles.updateDependencyVersions(
          doc,
          Collections.singleton(
              new ArtifactUpdate(
                  consumer.getGroupId(),
                  consumer.getArtifactId(),
                  consumer.getCurrentVersion(),
                  newVersion)));
      String updatedPom = doc.toXML();
      GHTree updatedTree =
          repository
              .createTree()
              .baseTree(tree.get().getSha())
              .add("pom.xml", updatedPom, false) // 'false' -> not executable
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
    }
  }

  private Optional<GHTreeEntry> findPomTreeEntry(GHRepository repository, GHTree root) {
    String[] parts = consumer.getPom().split("/");
    GHTree tree = root;
    GHTreeEntry entry = null;
    for (String part : parts) {
      entry = tree.getEntry(part);
      if (entry == null) {
        return Optional.empty();
      }
      try {
        tree = repository.getTree(entry.getSha());
      } catch (IOException e) {
        throw new RuntimeException("failed to walk tree for consumer=" + consumer, e);
      }
    }
    if (entry.getType().equals("blob")) {
      return Optional.of(entry);
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
