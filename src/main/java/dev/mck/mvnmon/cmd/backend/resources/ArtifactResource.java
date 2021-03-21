package dev.mck.mvnmon.cmd.backend.resources;

import dev.mck.mvnmon.api.maven.Artifact;
import dev.mck.mvnmon.sql.ArtifactDao;
import java.util.Optional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.jdbi.v3.core.Jdbi;

@Path("/")
@Produces({MediaType.APPLICATION_JSON})
public class ArtifactResource {
  private final Jdbi jdbi;

  public ArtifactResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Path("/artifact")
  @GET
  public Optional<Artifact> get(
      @QueryParam("group") @NotNull @NotBlank String group,
      @QueryParam("artifact") @NotNull @NotBlank String artifact) {
    var dao = jdbi.onDemand(ArtifactDao.class);
    return dao.get(group, artifact);
  }

  @Path("/artifacts")
  @GET
  public void scan(
      @QueryParam("limit") @DefaultValue("10") int limit,
      @QueryParam("cursor") @DefaultValue("0") int cursor) {
    var dao = jdbi.onDemand(ArtifactDao.class);
    dao.scan(limit, cursor);
  }
}
