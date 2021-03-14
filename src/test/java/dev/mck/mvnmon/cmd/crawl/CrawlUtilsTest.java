package dev.mck.mvnmon.cmd.crawl;

import dev.mck.mvnmon.cmd.crawl.CrawlUtils;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class CrawlUtilsTest {
  @Test
  public void getNewVersion_guava() {
    // from com.google.guava:guava
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

    var newVersion = CrawlUtils.getNewVersion("20.0-jre", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("30.1-jre");

    newVersion = CrawlUtils.getNewVersion("28.2-jre", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("30.1-jre");

    newVersion = CrawlUtils.getNewVersion("30.0-android", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("30.1-android");
  }

  @TestFactory
  public void getNewVersion_postgresql() {
    // from org.postgresql:postgresql
    List<String> latestVersions = new ArrayList<>();
    latestVersions.add("42.2.19.jre7");
    latestVersions.add("42.2.19.jre6");
    latestVersions.add("42.2.19");
    latestVersions.add("42.2.18.jre7");
    latestVersions.add("42.2.18");
    latestVersions.add("42.2.18.jre6");

    var newVersion = CrawlUtils.getNewVersion("42.2.18", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19");

    newVersion = CrawlUtils.getNewVersion("42.2.17", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19");

    newVersion = CrawlUtils.getNewVersion("42.2.18.jre7", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19.jre7");

    newVersion = CrawlUtils.getNewVersion("42.2.18.jre6", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19.jre6");

    newVersion = CrawlUtils.getNewVersion("42.2.19", latestVersions);
    assertThat(newVersion).isEmpty();
  }

  @TestFactory
  public void getNewVersion_junit_jupiter() {
    // from org.junit.jupiter:junit-jupiter-api
    List<String> latestVersions = new ArrayList<>();
    latestVersions.add("5.8.0-M1");
    latestVersions.add("5.7.1");
    latestVersions.add("5.6.3");
    latestVersions.add("5.7.0-RC1");
    latestVersions.add("5.7.0-M1");
    latestVersions.add("5.6.2");

    var newVersion = CrawlUtils.getNewVersion("5.7.1", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("5.8.0-M1");

    newVersion = CrawlUtils.getNewVersion("5.6.2", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("5.8.0-M1");

    newVersion = CrawlUtils.getNewVersion("4.0.0", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("5.8.0-M1");
  }
}
