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
import mck.mvnmon.cmd.schedule.ScheduleConfiguration;
import mck.mvnmon.cmd.update.UpdateConfiguration;
import mck.mvnmon.ipc.NatsFactory;
import org.jdbi.v3.core.Jdbi;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class MvnMonConfiguration extends Configuration {
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

  public Jdbi buildJdbi(Environment e) {
    var factory = new JdbiFactory();
    return factory.build(e, db, "db");
  }
}
