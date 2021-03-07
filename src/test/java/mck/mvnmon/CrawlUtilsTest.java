package mck.mvnmon;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import mck.mvnmon.api.MavenId;
import mck.mvnmon.crawl.CrawlUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class CrawlUtilsTest {
  @Test
  public void getNewVersion_guava() {
    MavenId mavenId = new MavenId("com.google.guava", "guava", "20.0-jre");
    List<String> latestVersions = new ArrayList<>();
    latestVersions.add("30.1-jre");
    latestVersions.add("30.1-android");
    latestVersions.add("30.0-jre");
    latestVersions.add("30.0-android");
    latestVersions.add("29.0-jre");
    latestVersions.add("29.0-android");
    latestVersions.add("28.2-jre");
    latestVersions.add("28.2-android");
    latestVersions.add("28.1-jre");
    latestVersions.add("28.1-android");
    var newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("30.1-jre");
  }

  @TestFactory
  public void getNewVersion_postgresql() {
    MavenId mavenId = new MavenId("org.postgresql", "postgresql", "42.2.18");
    List<String> latestVersions = new ArrayList<>();
    latestVersions.add("42.2.19.jre7");
    latestVersions.add("42.2.19.jre6");
    latestVersions.add("42.2.19");
    latestVersions.add("42.2.18.jre7");
    latestVersions.add("42.2.18");
    latestVersions.add("42.2.18.jre6");

    var newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19");

    mavenId = mavenId.withNewVersion("42.2.18.jre7");
    newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19.jre7");

    mavenId = mavenId.withNewVersion("42.2.18.jre6");
    newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19.jre6");
  }
}
