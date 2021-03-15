package dev.mck.mvnmon.cmd.crawler;

import static org.asynchttpclient.Dsl.*;

import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import dev.mck.mvnmon.nats.DispatcherManager;
import dev.mck.mvnmon.nats.Subjects;
import dev.mck.mvnmon.util.CloseableManager;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class CrawlerCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public CrawlerCommand(Application<MvnMonConfiguration> application) {
    super(application, "crawler", "Crawl scheduled maven ID-s to check for current version.");
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
        new CrawlerResponseListenerFactory(
            executor, nats, configuration.getCrawl().getMaxConcurrentRequests());
    var requestHandler = new CrawlerMessageHandler(httpClient, responseListenerFactory);

    var dispatcher = nats.createDispatcher(requestHandler);
    dispatcher.subscribe(Subjects.SCHEDULED, "crawl");
    environment.lifecycle().manage(new DispatcherManager(dispatcher));
  }
}
