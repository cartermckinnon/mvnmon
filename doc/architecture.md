# Architecture

`mvnmon` is a handful of programs packaged into a single binary. The programs
communicate with each other via
[`**nats-server**`](https://github.com/nats-io/nats-server).

```
            ┌───────────────────┐
            │                   │
            │  MavenIdProvider  │
            │                   │
            └─────────┬─────────┘
                      │
                      │
            ┌─────────┴─────────┐
            │                   │
            │     Scheduler     │
            │                   │
            └─────────┬─────────┘
                      │
                      │
            ┌─────────┴─────────┐
            │                   │
            │      Crawler      │
            │                   │
            └────┬────────┬─────┘
                 │        │
                 │        │
┌────────────────┴──┐  ┌──┴────────────────┐
│                   │  │                   │
│      Updater      │  │      Alerter      │
│                   │  │                   │
└───────────────────┘  └───────────────────┘
```

- **MavenIdProvider**
  - Source of Maven ID's (`group:artifact:version`) can be a local file (such as
    YAML or POM), a file at a remote URL (such as GitHub), or a database (such
    as postgres).
- **Scheduler**
  - Batch job executed at whatever frequency you'd like to check for new
    versions (such as every 12 hours). You can trigger this job using `cron` or
    a Kubernetes `CronJob`, for example. Schedulers operate on whatever
    MavenIdProvider(s) they are configured with, so you can have one scheduler
    for all of your MavenIdProviders, or several, or many; depending on your
    choice of orchestration.
- **Crawler**
  - Downloads the latest version of the Maven ID's. This component uses fully
    asynchronous I/O, so can handle many concurrent downloads. However, you may
    need to run multiple instances (such as via Kubernetes'
    `HorizontalPodAutoscaler`).
- **Updater**
  - If a newer version is available, updates the MavenIdProvider (such as
    opening a pull request, modifying a local file, or saving to a database).
    Similarly to the Crawler, you may need to run multiple instances.
- **Alerter**
  - If a newer version is available, send a notification (such as an email, SMS,
    or Slack). Like the two previous components, you may need to scale the
    Alerter.
