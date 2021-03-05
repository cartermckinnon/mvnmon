package mck.mvnmon.cmd;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import mck.mvnmon.MvnMonConfiguration;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.db.MvnMonDao;
import mck.mvnmon.ipc.Subjects;
import net.sourceforge.argparse4j.inf.Namespace;

@Slf4j
public class ScheduleCommand extends ConfiguredCommand<MvnMonConfiguration> {

  public ScheduleCommand() {
    super("schedule", "Queue all Maven artifacts for a version check.");
  }

  @Override
  protected void run(
      Bootstrap<MvnMonConfiguration> boot, Namespace args, MvnMonConfiguration configuration)
      throws Exception {
    Environment env = new Environment("schedule-command");
    var jdbi = configuration.buildJdbi(env);
    var dao = jdbi.onDemand(MvnMonDao.class);
    var nats = configuration.getNats().build();

    List<MavenId> results;
    long cursor = 0;
    long n = 0;

    final long start = System.currentTimeMillis();
    while (true) {
      results = dao.get(100, cursor);
      if (results.isEmpty()) {
        break;
      }
      for (MavenId result : results) {
        nats.publish(Subjects.SCHEDULED, result.toBytes());
      }
      n += results.size();
    }
    final long stop = System.currentTimeMillis();

    nats.close();

    LOG.info("scheduled {} id(s) in {} ms", n, (stop - start));
  }
}
