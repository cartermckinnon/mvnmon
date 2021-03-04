package mck.mvnmon;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import io.nats.client.Connection;
import io.nats.client.Nats;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mck.mvnmon.nats.NatsConfiguration;
import mck.mvnmon.nats.NatsManager;
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
  private NatsConfiguration nats = new NatsConfiguration();

  public Jdbi buildJdbi(Environment e) {
    var factory = new JdbiFactory();
    return factory.build(e, db, "db");
  }

  public Connection buildNats(Environment e) throws Exception {
    var connection = Nats.connect(nats.concatenateUrls());
    e.lifecycle().manage(new NatsManager(connection));
    return connection;
  }
}
