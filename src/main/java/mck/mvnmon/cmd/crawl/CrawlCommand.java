package mck.mvnmon.cmd.crawl;

import static org.asynchttpclient.Dsl.*;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import mck.mvnmon.MvnMonConfiguration;
import mck.mvnmon.cmd.ExtendedServerCommand;
import mck.mvnmon.nats.DispatcherManager;
import mck.mvnmon.nats.Subjects;
import mck.mvnmon.util.CloseableManager;

public class CrawlCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public CrawlCommand(Application<MvnMonConfiguration> application) {
    super(application, "crawl", "Crawl scheduled maven ID-s to check for current version.");
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    var nats = configuration.getNats().build(environment);

    // this should be registered on the environment before the http client,
    // so that it is shut down *after* the client is drained.
    var executor = environment.lifecycle().executorService("crawl-response-listener-%d").build();

    var httpClient = asyncHttpClient();
    environment.lifecycle().manage(new CloseableManager(httpClient));

    var responseListenerFactory =
        new CrawlResponseListenerFactory(
            executor, nats, configuration.getCrawl().getMaxConcurrentRequests());
    var requestHandler = new CrawlMessageHandler(httpClient, responseListenerFactory);

    var dispatcher = nats.createDispatcher(requestHandler);
    dispatcher.subscribe(Subjects.SCHEDULED, "crawl");
    environment.lifecycle().manage(new DispatcherManager(dispatcher));
  }
}
