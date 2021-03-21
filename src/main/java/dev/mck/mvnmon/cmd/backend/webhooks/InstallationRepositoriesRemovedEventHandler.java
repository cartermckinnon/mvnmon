package dev.mck.mvnmon.cmd.backend.webhooks;

import dev.mck.mvnmon.api.github.InstallationRepositoriesRemovedEvent;
import dev.mck.mvnmon.api.github.Repository;
import dev.mck.mvnmon.sql.RepositoryDao;
import dev.mck.mvnmon.util.Serialization;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallationRepositoriesRemovedEventHandler implements MessageHandler {

  private static final Logger LOG =
      LoggerFactory.getLogger(InstallationRepositoriesRemovedEventHandler.class);

  private final Jdbi jdbi;

  public InstallationRepositoriesRemovedEventHandler(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    var event =
        Serialization.deserialize(msg.getData(), InstallationRepositoriesRemovedEvent.class);
    var dao = jdbi.onDemand(RepositoryDao.class);
    for (Repository repository : event.getRepositoriesRemoved()) {
      dao.delete(repository.getId());
    }
    LOG.info(
        "removed repositories={} from installation={}",
        event.getRepositoriesRemoved(),
        event.getInstallation());
  }
}
