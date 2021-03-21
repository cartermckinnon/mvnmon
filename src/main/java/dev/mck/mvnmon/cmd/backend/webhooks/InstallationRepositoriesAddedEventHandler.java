package dev.mck.mvnmon.cmd.backend.webhooks;

import dev.mck.mvnmon.api.github.Installation;
import dev.mck.mvnmon.api.github.InstallationRepositoriesAddedEvent;
import dev.mck.mvnmon.api.github.Repository;
import dev.mck.mvnmon.sql.InstallationDao;
import dev.mck.mvnmon.util.Pair;
import java.util.Collection;
import org.jdbi.v3.core.Jdbi;

public class InstallationRepositoriesAddedEventHandler
    extends RepositoryInitHandler<InstallationRepositoriesAddedEvent> {

  public InstallationRepositoriesAddedEventHandler(Jdbi jdbi) {
    super(InstallationRepositoriesAddedEvent.class, jdbi);
  }

  @Override
  protected Pair<Installation, String> getInstallationAndToken(
      InstallationRepositoriesAddedEvent event) {
    var token = getJdbi().onDemand(InstallationDao.class).getToken(event.getInstallation().getId());
    if (token == null) {
      throw new IllegalStateException("failed to get installation token for event=" + event);
    }
    return new Pair<>(event.getInstallation(), token);
  }

  @Override
  protected Collection<Repository> getRepositories(InstallationRepositoriesAddedEvent event) {
    return event.getRepositoriesAdded();
  }
}
