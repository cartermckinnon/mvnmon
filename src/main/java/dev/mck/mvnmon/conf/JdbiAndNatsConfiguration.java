package dev.mck.mvnmon.conf;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import dev.mck.mvnmon.nats.NatsFactory;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.postgresql.Driver;

public class JdbiAndNatsConfiguration extends Configuration {
  public JdbiAndNatsConfiguration() {
    this.db.setDriverClass(Driver.class.getName());
    this.db.setUrl("jdbc:postgresql://localhost:5432/");
  }

  @NotNull @Valid private DataSourceFactory db = new DataSourceFactory();

  @NotNull @Valid private NatsFactory nats = new NatsFactory();

  public DataSourceFactory getDb() {
    return this.db;
  }

  public NatsFactory getNats() {
    return this.nats;
  }

  public void setDb(final DataSourceFactory db) {
    this.db = db;
  }

  public void setNats(final NatsFactory nats) {
    this.nats = nats;
  }

  @Override
  public String toString() {
    return toString(MoreObjects.toStringHelper(this));
  }

  public String toString(ToStringHelper helper) {
    return helper.add("nats", nats).add("db", db).toString();
  }
}
