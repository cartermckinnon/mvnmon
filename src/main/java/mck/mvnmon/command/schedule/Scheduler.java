package mck.mvnmon.command.schedule;

import io.nats.client.Connection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.api.maven.ArtifactWithId;
import mck.mvnmon.nats.Subjects;
import mck.mvnmon.sql.ArtifactDao;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class Scheduler implements Runnable {

  private final ScheduleConfiguration configuration;
  private final Jdbi jdbi;
  private final Connection nats;

  public Scheduler(ScheduleConfiguration configuration, Jdbi jdbi, Connection nats) {
    this.configuration = configuration;
    this.jdbi = jdbi;
    this.nats = nats;
  }

  @Override
  public void run() {
    var dao = jdbi.onDemand(ArtifactDao.class);

    List<ArtifactWithId> results;
    long cursor = 0;
    long n = 0;

    final long start = System.currentTimeMillis();
    while (true) {
      results = dao.scan(100, cursor);
      if (results.isEmpty()) {
        break; // table is empty or the previous batch was the end of the table
      }
      for (ArtifactWithId result : results) {
        nats.publish(Subjects.SCHEDULED, result.toBytes());
      }
      n += results.size();
      if (results.size() < configuration.getBatchSize()) {
        break; // we reached the end of the table
      }
    }
    final long stop = System.currentTimeMillis();

    LOG.info("scheduled {} id(s) in {} ms", n, (stop - start));
  }
}
