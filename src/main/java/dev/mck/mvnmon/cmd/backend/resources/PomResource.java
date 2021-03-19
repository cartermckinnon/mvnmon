package dev.mck.mvnmon.cmd.backend.resources;

import dev.mck.mvnmon.api.maven.Pom;
import dev.mck.mvnmon.sql.PomDao;
import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.jdbi.v3.core.Jdbi;

@Path("/poms")
@Produces({MediaType.APPLICATION_JSON})
public class PomResource {
  private final Jdbi jdbi;

  public PomResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @GET
  public List<Pom> get(@QueryParam("limit") @Min(1) @Nullable Integer limit) {
    if (limit == null) {
      limit = 100;
    }
    var dao = jdbi.onDemand(PomDao.class);
    return dao.get(limit);
  }
}
