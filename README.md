# `mvnmon`

`mvnmon` (Maven monitor) is a GitHub App that will monitor `pom.xml` files in
your repositories, and open pull requests to update their `<dependencies>`.

### Purpose

`mvnmon` is intended to make the routine maintenance of bumping dependency
versions painless. Pull requests will be opened with suggested updates. It is
the responsibility of the user to verify that the changes do not break the
stability of the build. Many existing CI integrations can address this; and
auto-merge when the pull request succeeds can be accomplished with other
softwares.

### Pre-requisites

At runtime:

- `postgresql`
- `nats-server`

At compile time:

- Java 16
- Docker
- Maven

### Build

```sh
scripts/build.sh
```

Then assemble a binary distributable:

```sh
scripts/dist.sh
```

`MVNMON_HOME` below is the root of the distribution (`dist/mvnmon-$VERSION/`).

### Usage

A public instance of `mvnmon` is coming! For now, you can register `mvnmon` as a
GitHub App under your own account, and run it yourself! It's pretty simple.
**Note** that `mvnmon` does not currently support GitHub enterprise
installations.

`mvnmon` is two programs: the `frontend` and the `backend`. The `fronend`
listens for GitHub webhooks, and the `backend` does...everything else.

#### 1. Create a GitHub App (Settings > Developer Settings > GitHub Apps) for `mvnmon`.

- Uncheck `Expire user authorization tokens`.
- Enter the `Webhook URL` (`https://FRONTEND/webhooks`).
- **Repository permissions**:
  - Contents: Read & write.
  - Metadata: Read-only.
- **Subscribe to events**:
  - Push

#### 2. Start the `backend`.

Modify [configuration/backend.yaml](configuration/backend.yaml) as necessary,
then:

```sh
$MVNMON_HOME/bin/mvnmon backend configuration/backend.yaml
```

The `backend` will receive work to do from the `frontend`, and will balance work
across instances, via `nats-server`.

#### 3. Start the `frontend`.

Modify [configuration/frontend.yaml](configuration/frontend.yaml) as necessary,
then:

```sh
$MVNMON_HOME/bin/mvnmon frontend configuration/frontend.yaml
```

Webhooks will be received at `/webhooks`, on port 8080 (by default).

#### 4. Run the scheduler, every once in a while (such as every 24 hours):

The user is responsible for the orchestration of the scheduler, for example with
`cron` or a Kubernetes `CronJob`.

```sh
curl -X POST $BACKEND:$ADMIN_PORT/tasks/scheduler
```

`ADMIN_PORT` is 8083, by default.
