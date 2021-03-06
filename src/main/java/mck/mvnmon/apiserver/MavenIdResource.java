package mck.mvnmon.apiserver;

import java.util.Optional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.db.MvnMonDao;
import org.jdbi.v3.core.Jdbi;

@Path("/api/v1/mavenId")
public class MavenIdResource {
  private final Jdbi jdbi;

  public MavenIdResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @GET
  public Optional<MavenId> get(
      @QueryParam("group") @NotNull @NotBlank String group,
      @QueryParam("artifact") @NotNull @NotBlank String artifact) {
    var dao = jdbi.onDemand(MvnMonDao.class);
    return dao.get(group, artifact);
  }
}
