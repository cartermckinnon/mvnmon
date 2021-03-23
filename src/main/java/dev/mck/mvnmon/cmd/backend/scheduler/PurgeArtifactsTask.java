package dev.mck.mvnmon.cmd.backend.scheduler;

import dev.mck.mvnmon.sql.ArtifactDao;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;

/**
 * Deletes artifacts which have no consumers; should be run before the scheduler every once in a
 * while, to minimize the number of crawled artifacts.
 */
public class PurgeArtifactsTask extends Task {

  private final Jdbi jdbi;

  public PurgeArtifactsTask(Jdbi jdbi) {
    super("purge-artifacts");
    this.jdbi = jdbi;
  }

  @Override
  public void execute(Map<String, List<String>> map, PrintWriter output) throws Exception {
    var dao = jdbi.onDemand(ArtifactDao.class);

    final long start = System.currentTimeMillis();
    int purged = dao.deleteArtifactsWithoutConsumers();
    final long stop = System.currentTimeMillis();

    String msg = String.format("purged %d orphaned artifact(s) in %d ms", purged, (stop - start));
    output.write(msg);
  }
}
