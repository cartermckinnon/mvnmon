package dev.mck.mvnmon.cmd.backend.webhooks;

import dev.mck.mvnmon.api.github.InstallationDeletedEvent;
import dev.mck.mvnmon.nats.TypedHandler;
import dev.mck.mvnmon.sql.InstallationDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallationDeletedEventHandler extends TypedHandler<InstallationDeletedEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(InstallationDeletedEventHandler.class);

  private final Jdbi jdbi;

  public InstallationDeletedEventHandler(Jdbi jdbi) {
    super(InstallationDeletedEvent.class);
    this.jdbi = jdbi;
  }

  @Override
  protected void handlePayload(InstallationDeletedEvent event) throws Exception {
    var dao = jdbi.onDemand(InstallationDao.class);
    if (dao.delete(event.getInstallation().getId())) {
      LOG.info("deleted installation={}", event.getInstallation());
    }
  }
}
