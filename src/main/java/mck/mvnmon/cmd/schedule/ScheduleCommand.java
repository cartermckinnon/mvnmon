package mck.mvnmon.cmd.schedule;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.MvnMonConfiguration;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.cmd.LifecycleManagedCommand;
import mck.mvnmon.db.MvnMonDao;
import mck.mvnmon.ipc.Subjects;
import net.sourceforge.argparse4j.inf.Namespace;

@Slf4j
public class ScheduleCommand extends LifecycleManagedCommand<MvnMonConfiguration> {

  public ScheduleCommand(Application<MvnMonConfiguration> application) {
    super(application, "schedule", "Queue all Maven artifacts for a version check.");
  }

  @Override
  protected void runManaged(
      Environment environment, Namespace namespace, MvnMonConfiguration configuration)
      throws Exception {
    var jdbi = configuration.buildJdbi(environment);
    var dao = jdbi.onDemand(MvnMonDao.class);
    var nats = configuration.getNats().build(environment);

    List<MavenId> results;
    long cursor = 0;
    long n = 0;

    final long start = System.currentTimeMillis();
    while (true) {
      results = dao.scan(100, cursor);
      if (results.isEmpty()) {
        break; // table is empty or the previous batch was the end of the table
      }
      for (MavenId result : results) {
        nats.publish(Subjects.SCHEDULED, result.toBytes());
      }
      n += results.size();
      if (results.size() < configuration.getSchedule().getBatchSize()) {
        break; // we reached the end of the table
      }
    }
    final long stop = System.currentTimeMillis();

    LOG.info("scheduled {} id(s) in {} ms", n, (stop - start));
  }
}
