package dev.mck.mvnmon.cmd.backend.scheduler;

import dev.mck.mvnmon.api.maven.ArtifactWithId;
import dev.mck.mvnmon.nats.Subjects;
import dev.mck.mvnmon.sql.ArtifactDao;
import dev.mck.mvnmon.util.Serialization;
import io.dropwizard.servlets.tasks.Task;
import io.nats.streaming.StreamingConnection;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;

public class SchedulerTask extends Task {

  private final SchedulerConfiguration configuration;
  private final Jdbi jdbi;
  private final StreamingConnection nats;

  public SchedulerTask(SchedulerConfiguration configuration, Jdbi jdbi, StreamingConnection nats) {
    super("scheduler");
    this.configuration = configuration;
    this.jdbi = jdbi;
    this.nats = nats;
  }

  @Override
  public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
    var dao = jdbi.onDemand(ArtifactDao.class);

    final long start = System.currentTimeMillis();
    List<ArtifactWithId> results;
    long cursor = 0;
    long n = 0;
    while (true) {
      results = dao.scan(100, cursor);
      if (results.isEmpty()) {
        break; // table is empty or the previous batch was the end of the table
      }
      for (ArtifactWithId result : results) {
        nats.publish(Subjects.SCHEDULED, Serialization.serializeAsBytes(result));
      }
      n += results.size();
      if (results.size() < configuration.getBatchSize()) {
        break; // we reached the end of the table
      }
    }
    final long stop = System.currentTimeMillis();

    String msg = String.format("scheduled %d artifact(s) in %d ms", n, (stop - start));
    output.write(msg);
  }
}
