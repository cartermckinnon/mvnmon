package mck.mvnmon.sql;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import mck.mvnmon.api.MavenArtifact;
import mck.mvnmon.source.MavenArtifactSource;
import org.jdbi.v3.core.Jdbi;

/**
 * SQL-database-backed source of MavenId-s.
 *
 * @see MavenArtifactDao
 */
public class SqlMavenArtifactSource implements MavenArtifactSource {

  private final Jdbi jdbi;
  private final int batchSize;

  public SqlMavenArtifactSource(Jdbi jdbi, int batchSize) {
    this.jdbi = jdbi;
    this.batchSize = batchSize;
  }

  @Override
  public Iterator<MavenArtifact> get() {
    return new MavenIdIterator(jdbi.onDemand(MavenArtifactDao.class), batchSize);
  }

  private static class MavenIdIterator implements Iterator<MavenArtifact> {

    private final MavenArtifactDao dao;
    private final int batchSize;
    private final Queue<MavenArtifact> queue;
    private boolean finished;
    private long cursor;

    private MavenIdIterator(MavenArtifactDao dao, int batchSize) {
      this.dao = dao;
      this.batchSize = batchSize;
      this.queue = new LinkedList<>();
      this.finished = true;
    }

    @Override
    public boolean hasNext() {
      getMore();
      return !queue.isEmpty();
    }

    @Override
    public MavenArtifact next() {
      if (queue.isEmpty()) {
        throw new NoSuchElementException(
            "there are no more maven id-s, you should be checking hashNext");
      }
      return queue.poll();
    }

    private void getMore() {
      if (queue.isEmpty() || finished) {
        return;
      }
      var batch = dao.scan(batchSize, cursor);
      queue.addAll(batch);
      finished = batch.size() < batchSize;
    }
  }
}
