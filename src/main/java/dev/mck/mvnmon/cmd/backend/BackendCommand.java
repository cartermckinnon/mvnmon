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
import dev.mck.mvnmon.cmd.backend.scheduler.PurgeArtifactsTask;
import dev.mck.mvnmon.cmd.backend.scheduler.SchedulerTask;
import dev.mck.mvnmon.cmd.backend.updater.UpdaterMessageHandler;
import dev.mck.mvnmon.cmd.backend.webhooks.InstallationCreatedEventHandler;
import dev.mck.mvnmon.cmd.backend.webhooks.InstallationDeletedEventHandler;
import dev.mck.mvnmon.cmd.backend.webhooks.InstallationRepositoriesAddedEventHandler;
import dev.mck.mvnmon.cmd.backend.webhooks.InstallationRepositoriesRemovedEventHandler;
import dev.mck.mvnmon.cmd.backend.webhooks.PushEventHandler;
import dev.mck.mvnmon.nats.Subjects;
import dev.mck.mvnmon.nats.SubscriptionManager;
import dev.mck.mvnmon.util.CloseableManager;
import io.dropwizard.setup.Environment;
import io.nats.streaming.SubscriptionOptions;

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

    var nats = configuration.getNats().build("backend", environment);

    // pull requester
    var pullRequesterSubscription =
        nats.subscribe(
            Subjects.CRAWLED,
            "backend",
            new PullRequesterMessageHandler(jdbi, pullRequesterExecutor),
            new SubscriptionOptions.Builder()
                .durableName("pull-requester")
                .startWithLastReceived()
                .build());
    environment.lifecycle().manage(new SubscriptionManager(pullRequesterSubscription));

    // updater
    var updaterHandler =
        new UpdaterMessageHandler(
            jdbi,
            configuration.getUpdater().getBatchSize(),
            configuration.getUpdater().getInterval(),
            updaterExecutor);
    environment.lifecycle().manage(new CloseableManager(updaterHandler));
    var updaterSubscription =
        nats.subscribe(
            Subjects.CRAWLED,
            "backend",
            updaterHandler,
            new SubscriptionOptions.Builder()
                .durableName("updater")
                .startWithLastReceived()
                .build());
    environment.lifecycle().manage(new SubscriptionManager(updaterSubscription));

    // crawler
    var httpClient = asyncHttpClient();
    environment.lifecycle().manage(new CloseableManager(httpClient));
    var crawlerResponseListenerFactory =
        new CrawlerResponseListenerFactory(
            crawlerExecutor, nats, configuration.getCrawler().getMaxConcurrentRequests());
    var crawlerSubscription =
        nats.subscribe(
            Subjects.SCHEDULED,
            "backend",
            new CrawlerMessageHandler(httpClient, crawlerResponseListenerFactory),
            new SubscriptionOptions.Builder()
                .durableName("crawler")
                .startWithLastReceived()
                .build());
    environment.lifecycle().manage(new SubscriptionManager(crawlerSubscription));

    // webhook handlers
    //  - push
    var pushWebhookSubscription =
        nats.subscribe(
            Subjects.hook("push"),
            "backend",
            new PushEventHandler(jdbi),
            new SubscriptionOptions.Builder()
                .durableName("push-webhook-handler")
                .startWithLastReceived()
                .build());
    environment.lifecycle().manage(new SubscriptionManager(pushWebhookSubscription));
    // - installation created
    var installationCreatedSubscription =
        nats.subscribe(
            Subjects.hook("installation", "created"),
            "backend",
            new InstallationCreatedEventHandler(
                jdbi, configuration.getPrivateKeyFile(), configuration.getAppId()),
            new SubscriptionOptions.Builder()
                .durableName("installation-created-webhook-handler")
                .startWithLastReceived()
                .build());
    environment.lifecycle().manage(new SubscriptionManager(installationCreatedSubscription));
    // - installation deleted
    var installationDeletedSubscription =
        nats.subscribe(
            Subjects.hook("installation", "deleted"),
            "backend",
            new InstallationDeletedEventHandler(jdbi),
            new SubscriptionOptions.Builder()
                .durableName("installation-deleted-webhook-handler")
                .startWithLastReceived()
                .build());
    environment.lifecycle().manage(new SubscriptionManager(installationDeletedSubscription));
    // - installation repositories added
    var repositoriesAddedSubscription =
        nats.subscribe(
            Subjects.hook("installation_repositories", "added"),
            "backend",
            new InstallationRepositoriesAddedEventHandler(jdbi),
            new SubscriptionOptions.Builder()
                .durableName("installation-repositories-added-webhook-handler")
                .startWithLastReceived()
                .build());
    environment.lifecycle().manage(new SubscriptionManager(repositoriesAddedSubscription));
    // - installation repositories removed
    var repositoriesRemovedSubscription =
        nats.subscribe(
            Subjects.hook("installation_repositories", "removed"),
            "backend",
            new InstallationRepositoriesRemovedEventHandler(jdbi),
            new SubscriptionOptions.Builder()
                .durableName("installation-repositories-removed-webhook-handler")
                .startWithLastReceived()
                .build());
    environment.lifecycle().manage(new SubscriptionManager(repositoriesRemovedSubscription));
    // end webhook handlers

    // tasks
    environment.admin().addTask(new SchedulerTask(configuration.getScheduler(), jdbi, nats));
    environment.admin().addTask(new PurgeArtifactsTask(jdbi));

    // resources
    environment.jersey().register(new ArtifactResource(jdbi));
    environment.jersey().register(new PomResource(jdbi));
    environment.jersey().register(new ConsumerResource(jdbi));
  }
}
