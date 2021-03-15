package dev.mck.mvnmon;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.mck.mvnmon.cmd.crawler.CrawlerConfiguration;
import dev.mck.mvnmon.cmd.pullrequester.PullRequesterConfiguration;
import dev.mck.mvnmon.cmd.scheduler.SchedulerConfiguration;
import dev.mck.mvnmon.cmd.updater.UpdaterConfiguration;
import dev.mck.mvnmon.cmd.webhook.WebhookConfiguration;
import dev.mck.mvnmon.nats.NatsFactory;
import dev.mck.mvnmon.sql.PostgresJdbiFactory;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jdbi.v3.core.Jdbi;
import org.postgresql.Driver;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class MvnMonConfiguration extends Configuration {

  public MvnMonConfiguration() {
    this.db.setDriverClass(Driver.class.getName());
    this.db.setUrl("jdbc:postgresql://localhost:5432/");
  }

  @Valid
  @NotNull
  @JsonProperty("db")
  private DataSourceFactory db = new DataSourceFactory();

  @Valid
  @NotNull
  @JsonProperty("nats")
  private NatsFactory nats = new NatsFactory();

  @Valid
  @NotNull
  @JsonProperty("updater")
  private UpdaterConfiguration update = new UpdaterConfiguration();

  @Valid
  @NotNull
  @JsonProperty("scheduler")
  private SchedulerConfiguration schedule = new SchedulerConfiguration();

  @Valid
  @NotNull
  @JsonProperty("crawler")
  private CrawlerConfiguration crawl = new CrawlerConfiguration();

  @Valid
  @NotNull
  @JsonProperty("webhook")
  private WebhookConfiguration webhook = new WebhookConfiguration();

  @Valid
  @NotNull
  @JsonProperty("pullRequester")
  private PullRequesterConfiguration pullRequest = new PullRequesterConfiguration();

  public Jdbi buildJdbi(Environment e) {
    var factory = new PostgresJdbiFactory();
    return factory.build(e, db, "db");
  }
}
