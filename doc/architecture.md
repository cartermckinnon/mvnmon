# Architecture

`mvnmon` is a handful of programs packaged into a single binary. The programs
communicate with each other via
[`**nats-server**`](https://github.com/nats-io/nats-server).

- **Database**
  - Stores the latest crawled versions for Maven artifacts, identified by their
    `groupId` and `artifactId`. These versions are refreshed regularly (once a
    day, ish). Projects which consume these artifacts are also stored. The
    relationship between the `artifacts` table and the `consumers` table is
    one-to-many. There could be different types of consumers, such as GitHub
    repositories or email addresses.
- **Scheduler**
  - Batch job executed at whatever frequency you'd like to check for new
    versions (such as every 24 hours). You can trigger this job using `cron` or
    a Kubernetes `CronJob`, for example. All artifacts in the Artifact Database
    are distributed to the Crawlers via NATS.
- **Crawler**
  - Downloads the latest version of the Maven ID's. This component uses fully
    asynchronous I/O, so can handle many concurrent downloads. However, you may
    need to run multiple instances (such as via Kubernetes'
    `HorizontalPodAutoscaler`). The Crawler determines whether a version update
    is available, and outputs a message to the Updater via NATS if so.
- **Updater**
  - If new versions are available, the Updater saves them to the Artifact
    Database and then determines which consumers need to be notified. It passes
    a NATS message to the relevant Alerter to perform the update (such as
    opening a pull request or sending an email). Similarly to the Crawler, you
    may need to run multiple instances.
- **Alerter**
  - If a newer version is available, send a notification (such as an email, SMS,
    or Slack). Like the two previous components, you may need to scale the
    Alerter.
