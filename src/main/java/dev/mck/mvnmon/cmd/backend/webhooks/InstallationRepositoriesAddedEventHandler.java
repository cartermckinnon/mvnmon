package dev.mck.mvnmon.cmd.backend.webhooks;

import dev.mck.mvnmon.api.github.InstallationRepositoriesAddedEvent;
import dev.mck.mvnmon.sql.InstallationDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallationRepositoriesAddedEventHandler
    extends RepositoryInitHandler<InstallationRepositoriesAddedEvent> {

  private static final Logger LOG =
      LoggerFactory.getLogger(InstallationRepositoriesAddedEventHandler.class);

  public InstallationRepositoriesAddedEventHandler(Jdbi jdbi) {
    super(InstallationRepositoriesAddedEvent.class, jdbi);
  }

  @Override
  public void handleEvent(InstallationRepositoriesAddedEvent event) throws Exception {
    var dao = getJdbi().onDemand(InstallationDao.class);
    String token = dao.getToken(event.getInstallation().getId());
    processPoms(event.getInstallation(), event.getRepositoriesAdded(), token);
    LOG.info(
        "added repositories={} installation={}",
        event.getRepositoriesAdded().size(),
        event.getInstallation().getId());
  }
}
