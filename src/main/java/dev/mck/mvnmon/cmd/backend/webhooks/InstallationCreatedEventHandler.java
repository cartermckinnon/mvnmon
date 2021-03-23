package dev.mck.mvnmon.cmd.backend.webhooks;

import com.google.common.io.Files;
import dev.mck.mvnmon.api.github.Installation;
import dev.mck.mvnmon.api.github.InstallationCreatedEvent;
import dev.mck.mvnmon.api.github.Repository;
import dev.mck.mvnmon.sql.InstallationDao;
import dev.mck.mvnmon.util.Pair;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.File;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.Date;
import org.jdbi.v3.core.Jdbi;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

public class InstallationCreatedEventHandler
    extends RepositoryInitHandler<InstallationCreatedEvent> {

  private final String privateKeyFile;
  private final String appId;

  public InstallationCreatedEventHandler(Jdbi jdbi, String privateKeyFile, String appId) {
    super(InstallationCreatedEvent.class, jdbi);
    this.privateKeyFile = privateKeyFile;
    this.appId = appId;
  }

  @Override
  protected Collection<Repository> getRepositories(InstallationCreatedEvent event) {
    return event.repositories();
  }

  protected Pair<Installation, String> getInstallationAndToken(InstallationCreatedEvent event)
      throws Exception {
    String jwt = createJWT(appId, privateKeyFile, 60000); // ttl of 60 seconds
    GitHub app = new GitHubBuilder().withJwtToken(jwt).build();
    GHAppInstallation installation = app.getApp().getInstallationById(event.installation().id());
    String token = installation.createToken().create().getToken();
    var installationDao = getJdbi().onDemand(InstallationDao.class);
    installationDao.insert(
        event.installation().id(), event.installation().account().login(), token);
    return new Pair<>(event.installation(), token);
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
