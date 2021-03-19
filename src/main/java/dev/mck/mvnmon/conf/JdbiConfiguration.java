package dev.mck.mvnmon.conf;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.postgresql.Driver;

public class JdbiConfiguration extends Configuration {
  public JdbiConfiguration() {
    this.db.setDriverClass(Driver.class.getName());
    this.db.setUrl("jdbc:postgresql://localhost:5432/");
  }

  @NotNull @Valid private DataSourceFactory db = new DataSourceFactory();

  public DataSourceFactory getDb() {
    return this.db;
  }

  public void setDb(final DataSourceFactory db) {
    this.db = db;
  }

  @Override
  public String toString() {
    return toString(MoreObjects.toStringHelper(this));
  }

  public String toString(ToStringHelper helper) {
    return helper.add("db", db).toString();
  }
}
