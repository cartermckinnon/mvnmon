package dev.mck.mvnmon.cmd.backend.webhooks;

import com.google.common.io.Files;
import de.pdark.decentxml.Document;
import dev.mck.mvnmon.api.github.InstallationCreatedEvent;
import dev.mck.mvnmon.api.github.Repository;
import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.sql.ArtifactConsumerDao;
import dev.mck.mvnmon.sql.ArtifactDao;
import dev.mck.mvnmon.sql.InstallationDao;
import dev.mck.mvnmon.sql.PomDao;
import dev.mck.mvnmon.sql.RepositoryDao;
import dev.mck.mvnmon.util.PomFiles;
import dev.mck.mvnmon.util.Serialization;
import dev.mck.mvnmon.util.XmlFiles;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import java.io.File;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallationCreatedEventHandler implements MessageHandler {

  private static final Logger LOG = LoggerFactory.getLogger(InstallationCreatedEventHandler.class);

  private final Jdbi jdbi;
  private final String privateKeyFile;
  private final String appId;

  public InstallationCreatedEventHandler(Jdbi jdbi, String privateKeyFile, String appId) {
    this.jdbi = jdbi;
    this.privateKeyFile = privateKeyFile;
    this.appId = appId;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    var event = Serialization.deserialize(msg.getData(), InstallationCreatedEvent.class);
    try {
      String token = createInstallationToken(event);
      processPoms(event, token);
    } catch (Exception e) {
      LOG.error("failed to handle event={}", msg, e);
    }
  }

  public String createInstallationToken(InstallationCreatedEvent event) throws Exception {
    String jwt = createJWT(appId, privateKeyFile, 60000); // ttl of 60 seconds
    GitHub app = new GitHubBuilder().withJwtToken(jwt).build();
    GHAppInstallation installation =
        app.getApp().getInstallationById(event.getInstallation().getId());
    String token = installation.createToken().create().getToken();
    var installationDao = jdbi.onDemand(InstallationDao.class);
    installationDao.insert(
        event.getInstallation().getId(), event.getInstallation().getAccount().getLogin(), token);
    return token;
  }

  public void processPoms(InstallationCreatedEvent event, String token) throws Exception {
    LOG.info("received event={}", event);
    var artifactDao = jdbi.onDemand(ArtifactDao.class);
    var consumerDao = jdbi.onDemand(ArtifactConsumerDao.class);
    var repositoryDao = jdbi.onDemand(RepositoryDao.class);
    var pomDao = jdbi.onDemand(PomDao.class);
    var github = GitHub.connectUsingOAuth(token);
    for (Repository repository : event.getRepositories()) {
      repositoryDao.insert(
          repository.getId(), repository.getName(), event.getInstallation().getId());
      List<GHContent> poms =
          github.searchContent().repo(repository.getName()).filename("pom.xml").list().toList();
      for (GHContent pom : poms) {
        Document doc = XmlFiles.parse(pom.read());
        var dependencies = PomFiles.getDependencies(doc);

        var artifacts =
            dependencies.stream()
                .map(d -> new Artifact(d.getGroupId(), d.getArtifactId(), d.getVersion()))
                .toList();
        artifactDao.insert(artifacts);

        var dependencyHash = PomFiles.hashDependencies(dependencies);
        long pomId = pomDao.insert(repository.getId(), pom.getPath(), dependencyHash);

        var consumers =
            dependencies.stream()
                .map(
                    d ->
                        new ArtifactConsumer(
                            pomId, d.getGroupId(), d.getArtifactId(), d.getVersion()))
                .toList();
        consumerDao.upsert(consumers);
      }
    }
  }

  private static String createJWT(String githubAppId, String privateKeyFile, long ttlMillis)
      throws Exception {
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;
    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);
    Key signingKey = loadPrivateKey(privateKeyFile);

    JwtBuilder builder =
        Jwts.builder()
            .setIssuedAt(now)
            .setIssuer(githubAppId)
            .signWith(signingKey, signatureAlgorithm);

    if (ttlMillis > 0) {
      long expMillis = nowMillis + ttlMillis;
      Date exp = new Date(expMillis);
      builder.setExpiration(exp);
    }

    return builder.compact();
  }

  private static PrivateKey loadPrivateKey(String filename) throws Exception {
    byte[] keyBytes = Files.toByteArray(new File(filename));
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(spec);
  }
}
