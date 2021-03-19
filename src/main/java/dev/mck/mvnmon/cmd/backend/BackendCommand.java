package dev.mck.mvnmon.cmd.backend;

import static dev.mck.mvnmon.sql.JdbiUtils.buildJdbi;
import static org.asynchttpclient.Dsl.asyncHttpClient;

import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import dev.mck.mvnmon.cmd.NoopApplication;
import dev.mck.mvnmon.cmd.backend.crawler.CrawlerMessageHandler;
import dev.mck.mvnmon.cmd.backend.crawler.CrawlerResponseListenerFactory;
import dev.mck.mvnmon.cmd.backend.pullrequester.PullRequesterMessageHandler;
import dev.mck.mvnmon.cmd.backend.resources.ArtifactResource;
import dev.mck.mvnmon.cmd.backend.resources.ConsumerResource;
import dev.mck.mvnmon.cmd.backend.resources.PomResource;
import dev.mck.mvnmon.cmd.backend.updater.UpdaterMessageHandler;
import dev.mck.mvnmon.cmd.backend.webhooks.InstallationCreatedEventHandler;
import dev.mck.mvnmon.cmd.backend.webhooks.PushEventHandler;
import dev.mck.mvnmon.cmd.backend.scheduler.SchedulerTask;
import dev.mck.mvnmon.nats.DispatcherManager;
import dev.mck.mvnmon.nats.PingMessageHandler;
import dev.mck.mvnmon.nats.Subjects;
import dev.mck.mvnmon.util.CloseableManager;
import io.dropwizard.setup.Environment;

public class BackendCommand extends ExtendedServerCommand<BackendConfiguration> {

  public BackendCommand() {
    super(
        new NoopApplication<>("webhook-backend", BackendConfiguration.class),
        "backend",
        "Daemon to handle GitHub webhook events.");
  }

  @Override
  protected void run(Environment environment, BackendConfiguration configuration) throws Exception {
    var jdbi = buildJdbi(environment, configuration.getDb());
    // the executors must be registered on the environment before the nats connection,
    // so that the nats connection is closed before the executors are shut down
    // BE AWARE that threads on these executors cannot utilize the nats connection!
    var updaterExecutor = environment.lifecycle().scheduledExecutorService("updater-%d").build();
    var pullRequesterExecutor =
        environment.lifecycle().executorService("pull-requester-%d").build();
    // this should be registered on the environment before the http client,
    // so that it is shut down *after* the client is drained.
    var crawlerExecutor = environment.lifecycle().executorService("crawler-%d").build();

    var nats = configuration.getNats().build(environment);

    // pull requester
    var pullRequesterHandler = new PullRequesterMessageHandler(jdbi, pullRequesterExecutor);
    var pullRequesterDispatcher =
        nats.createDispatcher(pullRequesterHandler)
            .subscribe(Subjects.CRAWLED, "backend-pull-requester");
    environment.lifecycle().manage(new DispatcherManager(pullRequesterDispatcher));

    // updater
    var updaterHandler =
        new UpdaterMessageHandler(
            jdbi,
            configuration.getUpdater().getBatchSize(),
            configuration.getUpdater().getInterval(),
            updaterExecutor);
    environment.lifecycle().manage(new CloseableManager(updaterHandler));
    var updaterDispatcher = nats.createDispatcher(updaterHandler);
    updaterDispatcher.subscribe(Subjects.CRAWLED, "backend-updater");
    environment.lifecycle().manage(new DispatcherManager(updaterDispatcher));

    // crawler
    var httpClient = asyncHttpClient();
    environment.lifecycle().manage(new CloseableManager(httpClient));
    var crawlerResponseListenerFactory =
        new CrawlerResponseListenerFactory(
            crawlerExecutor, nats, configuration.getCrawler().getMaxConcurrentRequests());
    var crawlerDispatcher =
        nats.createDispatcher(new CrawlerMessageHandler(httpClient, crawlerResponseListenerFactory))
            .subscribe(Subjects.SCHEDULED, "backend");
    environment.lifecycle().manage(new DispatcherManager(crawlerDispatcher));

    // webhook handlers
    //  - push
    var pushWebhookDispatcher =
        nats.createDispatcher(new PushEventHandler(jdbi))
            .subscribe(Subjects.hook("push"), "backend");
    environment.lifecycle().manage(new DispatcherManager(pushWebhookDispatcher));
    // - installation created
    var installationCreatedDispatcher =
        nats.createDispatcher(
                new InstallationCreatedEventHandler(
                    jdbi, configuration.getPrivateKeyFile(), configuration.getAppId()))
            .subscribe(Subjects.hook("installation", "created"), "backend");
    environment.lifecycle().manage(new DispatcherManager(installationCreatedDispatcher));
    // end webhook handlers

    // ping
    var pingDispatcher =
        nats.createDispatcher(new PingMessageHandler(nats)).subscribe(Subjects.BACKEND_PING);
    environment.lifecycle().manage(new DispatcherManager(pingDispatcher));

    // tasks
    environment.admin().addTask(new SchedulerTask(configuration.getScheduler(), jdbi, nats));

    // resources
    environment.jersey().register(new ArtifactResource(jdbi));
    environment.jersey().register(new PomResource(jdbi));
    environment.jersey().register(new ConsumerResource(jdbi));
  }
}
