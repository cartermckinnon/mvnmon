# Architecture

`mvnmon` is a two daemons, `frontend` and `backend` , packaged into a single
binary. They communicate with each other via
[ `nats-server` ](https://github.com/nats-io/nats-server), and the backend
stores data in Postgres. You can run multiple instances of each daemon for a
"highly available" deployment, or dynamically scale the daemons with something
like Kubernetes' `HorizontalPodAutoscaler` .

```
 ┌ ─ ─ ─ ─ ─ ─┐             ┌ ─ ─ ─ ─ ─ - - - ┐

 │  WEBHOOKS  │             │  MAVEN CENTRAL  │

 └─ ─ ─ ┬ ─ ─ ┘             └─ ─ ─ - ▲ ─ - - -┘
        │                            |
        │                            |
 ┌──────▼─────┐   ┌────────┐   ┌─────┴─────┐   ┌────────────┐
 │            │   │        │   │           │   │            │
 │  FRONTEND  ├───►  NATS  ├───►  BACKEND  ├───►  POSTGRES  │
 │            │   │        │   │           │   │            │
 └────────────┘   └────────┘   └─────┬─────┘   └────────────┘
                                     │
                                     │
                              ┌─ ─ ─ ▼- ─ ─ ─┐

                              │  GITHUB API  │

                              └ ─ ─ ─ ─ ─ ─ ─┘
```

---

## Frontend

Receives webhook events from GitHub, validates them, and publishes them to NATS
to be processed by the backend.

---

## Backend

Receives events from the frontend and queues work with NATS. Provides an HTTP
API for accessing stored metadata and triggering batch jobs.

_Webhook handlers:_

- **Installation events**

  When an installation is created or updated, its repositories are searched for
  `pom.xml` files, and the `<dependencies>` are registered as artifact
  consumers. A repository access token is generated for the installation and
  stored for later use. A fingerprint of each POM's dependencies is stored to
  prevent unnecessary work when changes are made to irrelevant areas of the POM.
  When an installation is deleted, its corresponding data is removed from the
  database.

- **Push events**

  When a push is made to a registered repository's default branch, any `pom.xml`
  modifications are applied accordingly to the database.

_Core components:_

- **Scheduler**

  Batch job executed at whatever frequency you'd like to check for new

  versions (such as every 24 hours). You can trigger this job using `cron` or a
  Kubernetes `CronJob` , for example. All registered artifacts are queued in
  NATS for crawling.

- **Crawler**

  Fetches the latest versions of an artifacts from Maven Central. This component
  uses asynchronous I/O, so can handle many concurrent downloads. The crawler
  publishes the versions to NATS if the list has changed.

- **Updater**

  Saves the crawled versions of an artifact to the database.

- **Pull requester**

  Receives latest versions from the crawler and opens a pull request for each
  consumer of the artifact (if their current version can be updated).

---

## Postgres

Stores metadata about artifact consumers and artifacts.

_Tables:_

- **`installations`**
- **`repositories`**
- **`poms`**
- **`consumers`**
- **`artifacts`**
