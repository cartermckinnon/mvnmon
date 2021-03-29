package dev.mck.mvnmon.nats;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import io.nats.client.Connection;
import io.nats.client.Nats;

@Testcontainers
public abstract class NatsTest {
  @Container
  public GenericContainer nats =
      new GenericContainer(DockerImageName.parse("nats:latest"))
          .withExposedPorts(4222)
          .waitingFor(
              new WaitAllStrategy()
                  .withStrategy(Wait.forListeningPort())
                  .withStrategy(
                      new LogMessageWaitStrategy()
                          .withRegEx(".*Server is ready.*"))
                  .withStartupTimeout(Duration.ofSeconds(10)));

  private Connection conn = null;

  @BeforeEach
  public void beforeEach() throws Exception {
    conn = Nats.connect("nats://" + nats.getHost() + ":" + nats.getMappedPort(4222));
  }

  @AfterEach
  public void afterEach() throws Exception {
    if (conn != null) {
      conn.close();
    }
  }

  public Connection getConnection() {
    return conn;
  }
}
