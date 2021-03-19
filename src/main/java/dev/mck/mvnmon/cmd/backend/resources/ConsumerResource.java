package dev.mck.mvnmon.cmd.backend.resources;

import dev.mck.mvnmon.api.maven.ArtifactConsumer;
import dev.mck.mvnmon.sql.ArtifactConsumerDao;
import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.jdbi.v3.core.Jdbi;

@Path("/consumers")
@Produces({MediaType.APPLICATION_JSON})
public class ConsumerResource {
  private final Jdbi jdbi;

  public ConsumerResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @GET
  public List<ArtifactConsumer> get(@QueryParam("limit") @Min(1) @Nullable Integer limit) {
    if (limit == null) {
      limit = 100;
    }
    var dao = jdbi.onDemand(ArtifactConsumerDao.class);
    return dao.get(limit);
  }
}
