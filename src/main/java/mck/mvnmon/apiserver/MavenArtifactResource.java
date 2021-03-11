package mck.mvnmon.apiserver;

import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import mck.mvnmon.api.MavenArtifact;
import mck.mvnmon.sql.MavenArtifactDao;
import org.jdbi.v3.core.Jdbi;

@Path("/api/v1/artifacts")
public class MavenArtifactResource {
  private final Jdbi jdbi;

  public MavenArtifactResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @GET
  public Optional<MavenArtifact> get(
      @QueryParam("group") @NotNull @NotBlank String group,
      @QueryParam("artifact") @NotNull @NotBlank String artifact) {
    var dao = jdbi.onDemand(MavenArtifactDao.class);
    return dao.get(group, artifact);
  }

  @POST
  public void insert(@NotNull @Valid MavenArtifact mavenArtifact) {
    var dao = jdbi.onDemand(MavenArtifactDao.class);
    dao.insert(mavenArtifact);
  }
}
