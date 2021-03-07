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

    mavenId = mavenId.withNewVersion("28.2-jre");
    newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("30.1-jre");

    mavenId = mavenId.withNewVersion("30.0-android");
    newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("30.1-android");
  }

  @TestFactory
  public void getNewVersion_postgresql() {
    List<String> latestVersions = new ArrayList<>();
    latestVersions.add("42.2.19.jre7");
    latestVersions.add("42.2.19.jre6");
    latestVersions.add("42.2.19");
    latestVersions.add("42.2.18.jre7");
    latestVersions.add("42.2.18");
    latestVersions.add("42.2.18.jre6");

    MavenId mavenId = new MavenId("org.postgresql", "postgresql", "42.2.18");
    var newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19");

    mavenId = mavenId.withNewVersion("42.2.17");
    newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19");

    mavenId = mavenId.withNewVersion("42.2.18.jre7");
    newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19.jre7");

    mavenId = mavenId.withNewVersion("42.2.18.jre6");
    newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19.jre6");

    mavenId = mavenId.withNewVersion("42.2.19");
    newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isEmpty();
  }

  @TestFactory
  public void getNewVersion_junit() {
    MavenId mavenId = new MavenId("org.junit.jupiter", "junit-jupiter-api", "5.7.1");
    List<String> latestVersions = new ArrayList<>();
    latestVersions.add("5.8.0-M1");
    latestVersions.add("5.7.1");
    latestVersions.add("5.6.3");
    latestVersions.add("5.7.0-RC1");
    latestVersions.add("5.7.0-M1");
    latestVersions.add("5.6.2");

    var newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("5.8.0-M1");

    mavenId = mavenId.withNewVersion("5.6.2");
    newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("5.8.0-M1");

    mavenId = mavenId.withNewVersion("4.0.0");
    newVersion = CrawlUtils.getNewVersion(mavenId, latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("5.8.0-M1");
  }
}
