package dev.mck.mvnmon.cmd.pullrequester;

import dev.mck.mvnmon.MvnMonConfiguration;
import dev.mck.mvnmon.cmd.ExtendedServerCommand;
import dev.mck.mvnmon.nats.DispatcherManager;
import dev.mck.mvnmon.nats.Subjects;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.kohsuke.github.GitHub;

public class PullRequesterCommand extends ExtendedServerCommand<MvnMonConfiguration> {

  public PullRequesterCommand(Application<MvnMonConfiguration> application) {
    super(application, "pull-requester", "Open pull requests for updated dependencies.");
  }

  @Override
  protected void run(Environment environment, MvnMonConfiguration configuration) throws Exception {
    var jdbi = configuration.buildJdbi(environment);
    var github = GitHub.connectUsingOAuth(configuration.getPullRequest().getAccessKey());
    var executor = environment.lifecycle().executorService("pull-requester-%d").build();
    var messageHandler = new PullRequesterMessageHandler(jdbi, github, executor);
    var nats = configuration.getNats().build(environment);
    var dispatcher = nats.createDispatcher(messageHandler);
    dispatcher.subscribe(Subjects.CRAWLED, "pull-requester");
    environment.lifecycle().manage(new DispatcherManager(dispatcher));
  }
}
