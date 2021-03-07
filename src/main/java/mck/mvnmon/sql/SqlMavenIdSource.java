package mck.mvnmon.sql;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.source.MavenIdSource;
import org.jdbi.v3.core.Jdbi;

/**
 * SQL-database-backed source of MavenId-s.
 *
 * @see MavenIdDao
 */
public class SqlMavenIdSource implements MavenIdSource {

  private final Jdbi jdbi;
  private final int batchSize;

  public SqlMavenIdSource(Jdbi jdbi, int batchSize) {
    this.jdbi = jdbi;
    this.batchSize = batchSize;
  }

  @Override
  public Iterator<MavenId> get() {
    return new MavenIdIterator(jdbi.onDemand(MavenIdDao.class), batchSize);
  }

  private static class MavenIdIterator implements Iterator<MavenId> {

    private final MavenIdDao dao;
    private final int batchSize;
    private final Queue<MavenId> queue;
    private boolean finished;
    private long cursor;

    private MavenIdIterator(MavenIdDao dao, int batchSize) {
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
    public MavenId next() {
      if (queue.isEmpty()) {
        throw new NoSuchElementException(
            "there are no more maven id-s, you should be checking hashNext");
      }
      return queue.poll();
    }

    public void getMore() {
      if (queue.isEmpty() || finished) {
        return;
      }
      var batch = dao.scan(batchSize, cursor);
      queue.addAll(batch);
      finished = batch.size() < batchSize;
    }
  }
}
