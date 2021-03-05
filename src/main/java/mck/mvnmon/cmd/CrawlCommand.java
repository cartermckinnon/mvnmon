package mck.mvnmon.cmd;

import static org.asynchttpclient.Dsl.*;

import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import mck.mvnmon.MvnMonConfiguration;
import mck.mvnmon.crawl.CrawlHandler;
import mck.mvnmon.ipc.NatsManager;
import mck.mvnmon.ipc.Subjects;
import mck.mvnmon.util.CloseableManager;
import net.sourceforge.argparse4j.inf.Namespace;

public class CrawlCommand extends EnvironmentCommand<MvnMonConfiguration> {

  public CrawlCommand(Application<MvnMonConfiguration> application) {
    super(application, "crawl", "Crawl scheduled maven ID-s to check for current version.");
  }

  @Override
  protected void run(
      Environment environment, Namespace namespace, MvnMonConfiguration configuration)
      throws Exception {
    var nats = configuration.getNats().build(environment);
    environment.lifecycle().manage(new NatsManager(nats));

    var httpClient = asyncHttpClient();
    environment.lifecycle().manage(new CloseableManager(httpClient));

    nats.createDispatcher(new CrawlHandler()).subscribe(Subjects.SCHEDULED, "crawl");
  }
}
