package dev.mck.mvnmon.sql;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;

public enum JdbiUtils {
  INSTANCE;

  public static final Jdbi buildJdbi(Environment e, DataSourceFactory dataSourceFactory) {
    PostgresJdbiFactory factory = new PostgresJdbiFactory();
    return factory.build(e, dataSourceFactory, "postgres");
  }
}
