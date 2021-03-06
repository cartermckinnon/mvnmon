package mck.mvnmon.cmd.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import mck.mvnmon.api.MavenId;
import org.jdbi.v3.core.Jdbi;

public class UpdateBatcher implements Runnable {

  private final Jdbi jdbi;
  private final Queue<MavenId> queue;
  private final int batchSize;

  public UpdateBatcher(Jdbi jdbi, int batchSize) {
    this.jdbi = jdbi;
    this.queue = new ConcurrentLinkedQueue<>();
    this.batchSize = batchSize;
  }

  public void queueMavenId(MavenId mavenId) {
    queue.add(mavenId);
  }

  @Override
  public void run() {
    List<MavenId> batch = new ArrayList<>(batchSize);
    MavenId mavenId;
    for (int i = 0; i < batchSize; i++) {
      mavenId = queue.poll();
      if (mavenId == null) {
        break;
      }
      batch.add(mavenId);
    }
    if (!batch.isEmpty()) {}
  }

  public int queueSize() {
    return queue.size();
  }
}
