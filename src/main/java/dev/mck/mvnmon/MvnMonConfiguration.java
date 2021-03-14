package dev.mck.mvnmon;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import dev.mck.mvnmon.cmd.crawl.CrawlConfiguration;
import dev.mck.mvnmon.cmd.schedule.ScheduleConfiguration;
import dev.mck.mvnmon.cmd.update.UpdateConfiguration;
import dev.mck.mvnmon.cmd.webhookserver.WebhookConfiguration;
import dev.mck.mvnmon.nats.NatsFactory;
import dev.mck.mvnmon.sql.PostgresJdbiFactory;
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
  @JsonProperty("update")
  private UpdateConfiguration update = new UpdateConfiguration();

  @Valid
  @NotNull
  @JsonProperty("schedule")
  private ScheduleConfiguration schedule = new ScheduleConfiguration();

  @Valid
  @NotNull
  @JsonProperty("crawl")
  private CrawlConfiguration crawl = new CrawlConfiguration();

  @Valid
  @NotNull
  @JsonProperty("webhook")
  private WebhookConfiguration webhook = new WebhookConfiguration();

  public Jdbi buildJdbi(Environment e) {
    var factory = new PostgresJdbiFactory();
    return factory.build(e, db, "db");
  }
}
