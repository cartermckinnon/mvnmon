package mck.mvnmon;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mck.mvnmon.crawl.CrawlConfiguration;
import mck.mvnmon.ipc.NatsFactory;
import mck.mvnmon.schedule.ScheduleConfiguration;
import mck.mvnmon.update.UpdateConfiguration;
import org.jdbi.v3.core.Jdbi;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class MvnMonConfiguration extends Configuration {

  public MvnMonConfiguration() {
    this.db.setDriverClass("org.postgres.Driver");
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

  public Jdbi buildJdbi(Environment e) {
    var factory = new JdbiFactory();
    return factory.build(e, db, "db");
  }
}
