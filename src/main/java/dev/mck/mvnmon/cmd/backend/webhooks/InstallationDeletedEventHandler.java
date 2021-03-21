package dev.mck.mvnmon.cmd.backend.webhooks;

import dev.mck.mvnmon.api.github.InstallationDeletedEvent;
import dev.mck.mvnmon.sql.InstallationDao;
import dev.mck.mvnmon.util.Serialization;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallationDeletedEventHandler implements MessageHandler {

  private static final Logger LOG = LoggerFactory.getLogger(InstallationDeletedEventHandler.class);

  private final Jdbi jdbi;

  public InstallationDeletedEventHandler(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    var event = Serialization.deserialize(msg.getData(), InstallationDeletedEvent.class);
    var dao = jdbi.onDemand(InstallationDao.class);
    if (dao.delete(event.getInstallation().getId())) {
      LOG.info("deleted installation={}", event.getInstallation());
    }
  }
}
