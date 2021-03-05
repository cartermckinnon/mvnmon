package mck.mvnmon.crawl;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrawlHandler implements MessageHandler {

  @Override
  public void onMessage(Message msg) throws InterruptedException {
    LOG.info("received message: '{}'", new String(msg.getData(), StandardCharsets.UTF_8));
  }
}
