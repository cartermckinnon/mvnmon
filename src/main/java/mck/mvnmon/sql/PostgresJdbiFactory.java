package mck.mvnmon.sql;

import io.dropwizard.jdbi3.JdbiFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;

public class PostgresJdbiFactory extends JdbiFactory {

  @Override
  protected void configure(final Jdbi jdbi) {
    super.configure(jdbi);
    jdbi.installPlugin(new PostgresPlugin());
  }
}
