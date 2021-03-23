package dev.mck.mvnmon.cmd.backend.webhooks;

import dev.mck.mvnmon.api.github.InstallationRepositoriesRemovedEvent;
import dev.mck.mvnmon.api.github.Repository;
import dev.mck.mvnmon.nats.TypedHandler;
import dev.mck.mvnmon.sql.RepositoryDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallationRepositoriesRemovedEventHandler
    extends TypedHandler<InstallationRepositoriesRemovedEvent> {

  private static final Logger LOG =
      LoggerFactory.getLogger(InstallationRepositoriesRemovedEventHandler.class);

  private final Jdbi jdbi;

  public InstallationRepositoriesRemovedEventHandler(Jdbi jdbi) {
    super(InstallationRepositoriesRemovedEvent.class);
    this.jdbi = jdbi;
  }

  @Override
  protected void handlePayload(InstallationRepositoriesRemovedEvent event) {
    var dao = jdbi.onDemand(RepositoryDao.class);
    for (Repository repository : event.getRepositoriesRemoved()) {
      dao.delete(repository.id());
    }
    LOG.info(
        "removed repositories={} from installation={}",
        event.getRepositoriesRemoved(),
        event.getInstallation());
  }
}
