package dev.mck.mvnmon.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/*
import static mck.mvnmon.util.XmlFiles.findElementsWithName;
import static mck.mvnmon.util.XmlFiles.firstChild;
import static mck.mvnmon.util.XmlFiles.firstChildTextContent;
import static mck.mvnmon.util.XmlFiles.parseXmlFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.pdark.decentxml.Document;
import de.pdark.decentxml.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
*/
// @Slf4j
public class PomFilesTest {

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

    var newVersion = PomFiles.getNewVersion("20.0-jre", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("30.1-jre");

    newVersion = PomFiles.getNewVersion("28.2-jre", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("30.1-jre");

    newVersion = PomFiles.getNewVersion("30.0-android", latestVersions);
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

    var newVersion = PomFiles.getNewVersion("42.2.18", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19");

    newVersion = PomFiles.getNewVersion("42.2.17", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19");

    newVersion = PomFiles.getNewVersion("42.2.18.jre7", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19.jre7");

    newVersion = PomFiles.getNewVersion("42.2.18.jre6", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("42.2.19.jre6");

    newVersion = PomFiles.getNewVersion("42.2.19", latestVersions);
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

    var newVersion = PomFiles.getNewVersion("5.7.1", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("5.8.0-M1");

    newVersion = PomFiles.getNewVersion("5.6.2", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("5.8.0-M1");

    newVersion = PomFiles.getNewVersion("4.0.0", latestVersions);
    assertThat(newVersion).isPresent().get().isEqualTo("5.8.0-M1");
  }

  /*
  String fmpVersion = "9.0.0";
  String assertJVersion = "9.1.0";
  String fabric8Version = "9.2.0";
  String springBootVersion = "9.3.0";

  protected static void assertChangesValid(
      File file, Document doc, List<DependencyVersionChange> changes) {
    for (DependencyVersionChange change : changes) {
      if (Objects.equal(MavenScopes.PLUGIN, change.getScope())) {
        assertPluginVersionChanged(file, doc, change);
      } else {
        assertDependencyVersionChanged(file, doc, change);
      }
    }
  }

  protected static void assertPluginVersionChanged(
      File file, Document doc, DependencyVersionChange change) {
    boolean found = false;
    List<Element> elements = findElementsWithName(doc.getRootElement(), "plugin");
    for (Element element : elements) {
      String groupId = firstChildTextContent(element, "groupId");
      String artifactId = firstChildTextContent(element, "artifactId");
      String version = firstChildTextContent(element, "version");
      if (Strings.notEmpty(groupId) && Strings.notEmpty(artifactId) && Strings.notEmpty(version)) {
        if (change.matches(groupId, artifactId)) {
          found = true;
          if (!version.startsWith("$")) {
            LOG.info(
                "File " + file + " has plugin " + change.getDependency() + " version: " + version);
            assertThat(version)
                .describedAs("File " + file + " plugin version for " + change.getDependency())
                .isEqualTo(change.getVersion());
          }
        }
      }
    }
    assertThat(found)
        .describedAs(
            "File "
                + file
                + " does not have plugin "
                + change.getDependency()
                + " version: "
                + change.getVersion())
        .isTrue();
  }

  protected static void assertDependencyVersionChanged(
      File file, Document doc, DependencyVersionChange change) {
    boolean found = false;
    List<Element> elements = findElementsWithName(doc.getRootElement(), "dependency");
    for (Element element : elements) {
      String groupId = firstChildTextContent(element, "groupId");
      String artifactId = firstChildTextContent(element, "artifactId");
      String version = firstChildTextContent(element, "version");
      if (Strings.notEmpty(groupId) && Strings.notEmpty(artifactId) && Strings.notEmpty(version)) {
        if (change.matches(groupId, artifactId)) {
          found = true;
          if (!version.startsWith("$")) {
            LOG.info(
                "File "
                    + file
                    + " has dependency "
                    + change.getDependency()
                    + " version: "
                    + version);
            assertThat(version)
                .describedAs("File " + file + " dependency version for " + change.getDependency())
                .isEqualTo(change.getVersion());
          }
        }
      }
    }
  }

  protected static void assertPropertiesValid(
      File file, Document doc, Map<String, String> propertyVersions) {
    Element properties = firstChild(doc.getRootElement(), "properties");
    if (properties != null) {
      for (Map.Entry<String, String> entry : propertyVersions.entrySet()) {
        String propertyName = entry.getKey();
        String propertyValue = entry.getValue();
        assertPropertyEqualsIfExists(file, properties, propertyName, propertyValue);
      }
    }
  }

  protected static void assertPropertyEqualsIfExists(
      File file, Element properties, String propertyName, String expectedValue) {
    String value = firstChildTextContent(properties, propertyName);
    if (value != null) {
      LOG.info("File " + file + " has property " + propertyName + " = " + value);
      assertEquals("File " + file + " property " + propertyName + " element", expectedValue, value);
    }
  }

  @Test
  public void testVersionReplacement() throws Exception {
    File outDir = Tests.copyPackageSources(getClass());

    LOG.info("Updating poms in " + outDir);

    File[] files = outDir.listFiles();
    assertNotNull("No output files!", files);
    assertTrue("No output files!", files.length > 0);

    List<PomUpdateStatus> pomsToChange = new ArrayList<>();

    for (File file : files) {
      try {
        PomUpdateStatus pomUpdateStatus = PomUpdateStatus.createPomUpdateStatus(file);
        pomUpdateStatus.setRootPom(true);
        pomsToChange.add(pomUpdateStatus);
      } catch (Exception e) {
        fail("Failed to parse " + file, e);
      }
    }

    Map<String, String> propertyVersions = new HashMap<>();
    propertyVersions.put("assertj.version", assertJVersion);
    propertyVersions.put("fabric8.maven.plugin.version", fmpVersion);
    propertyVersions.put("fabric8.version", fabric8Version);
    propertyVersions.put("spring-boot.version", springBootVersion);

    // lets add some changes
    List<DependencyVersionChange> changes = new ArrayList<>();
    changes.add(
        new MavenDependencyVersionChange(
            "io.fabric8:fabric8-maven-plugin",
            fmpVersion,
            MavenScopes.PLUGIN,
            true,
            ElementProcessors.createFabric8MavenPluginElementProcessor()));

    changes.add(
        new DependencyVersionChange(
            Kind.MAVEN, "org.assertj:assertj-core", assertJVersion, MavenScopes.ARTIFACT));

    // BOM dependencies
    changes.add(
        new DependencyVersionChange(
            Kind.MAVEN,
            "io.fabric8:fabric8-project-bom-with-platform-deps",
            fabric8Version,
            MavenScopes.ARTIFACT));
    changes.add(
        new DependencyVersionChange(
            Kind.MAVEN,
            "org.springframework.boot:spring-boot-dependencies",
            springBootVersion,
            MavenScopes.ARTIFACT));

    PomHelper.updatePomVersions(pomsToChange, changes);

    for (File file : files) {
      Document doc;
      try {
        doc = parseXmlFile(file);
      } catch (Exception e) {
        fail("Failed to parse " + file + " due to " + e, e);
        continue;
      }

      assertPropertiesValid(file, doc, propertyVersions);
      assertChangesValid(file, doc, changes);
    }
  }
  */
}
