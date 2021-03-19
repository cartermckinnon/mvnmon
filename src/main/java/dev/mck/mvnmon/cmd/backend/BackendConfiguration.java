package dev.mck.mvnmon.cmd.backend;

import com.google.common.base.MoreObjects;
import dev.mck.mvnmon.cmd.backend.crawler.CrawlerConfiguration;
import dev.mck.mvnmon.cmd.backend.pullrequester.PullRequesterConfiguration;
import dev.mck.mvnmon.cmd.backend.updater.UpdaterConfiguration;
import dev.mck.mvnmon.cmd.backend.scheduler.SchedulerConfiguration;
import dev.mck.mvnmon.conf.JdbiAndNatsConfiguration;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class BackendConfiguration extends JdbiAndNatsConfiguration {
  @NotNull @NotBlank private String privateKeyFile = null;

  @NotNull @NotBlank private String appId = null;

  @NotNull @Valid
  private PullRequesterConfiguration pullRequester = new PullRequesterConfiguration();

  @NotNull @Valid private UpdaterConfiguration updater = new UpdaterConfiguration();

  @NotNull @Valid private CrawlerConfiguration crawler = new CrawlerConfiguration();

  @NotNull @Valid private SchedulerConfiguration scheduler = new SchedulerConfiguration();

  public String getPrivateKeyFile() {
    return this.privateKeyFile;
  }

  public String getAppId() {
    return appId;
  }

  public PullRequesterConfiguration getPullRequester() {
    return pullRequester;
  }

  public UpdaterConfiguration getUpdater() {
    return updater;
  }

  public CrawlerConfiguration getCrawler() {
    return crawler;
  }

  public SchedulerConfiguration getScheduler() {
    return scheduler;
  }

  public void setPrivateKeyFile(String privateKeyFile) {
    this.privateKeyFile = privateKeyFile;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setPullRequester(PullRequesterConfiguration pullRequester) {
    this.pullRequester = pullRequester;
  }

  public void setUpdater(UpdaterConfiguration updater) {
    this.updater = updater;
  }

  public void setCrawler(CrawlerConfiguration crawler) {
    this.crawler = crawler;
  }

  public void setScheduler(SchedulerConfiguration scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public String toString() {
    return super.toString(
        MoreObjects.toStringHelper(this)
            .add("privateKeyFile", privateKeyFile)
            .add("appId", appId)
            .add("pullRequester", pullRequester)
            .add("updater", updater)
            .add("crawler", crawler)
            .add("scheduler", scheduler));
  }
}
