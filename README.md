# `mvnmon`

`mvnmon` (Maven monitor) is a GitHub App that will monitor `pom.xml` files in your repositories, and open pull requests to update their `<dependencies>`.

### Pre-requisites

At runtime:
- `postgresql`
- `nats-server`

At compile time:
- Java 16
- Docker
- Maven

### Setup

A public instance of `mvnmon` is coming! For now, you can register `mvnmon` as a GitHub App under your own account, and run it yourself! It's pretty simple. Please note that I offer *no* support and `mvnmon` is actively under rapid development.

### Usage

`mvnmon` is two programs: the `frontend` and the `backend`. The `fronend` listens for GitHub webhooks, and the `backend` does...everything else.

1. Start the `frontend`:
```sh
$MVNMON_HOME/bin/mvnmon frontend configuration/frontend.yaml
```

Webhooks will be received at `/webhooks`, on port 8080 (by default). Modify [configuration/frontend.yaml](configuration/frontend.yaml) as necessary.

2. Start the `backend`:
```sh
$MVNMON_HOME/bin/mvnmon backend configuration/backend.yaml
```

The `backend` will receive work to do from the `frontend` and the scheduler via `nats-server`. Modify [configuration/backend.yaml](configuration/backend.yaml) as necessary.

3. Run the scheduler, every once in a while (such as every 24 hours):
```sh
curl -X POST $BACKEND:$ADMIN_PORT/tasks/scheduler
```

`ADMIN_PORT` is 8083, by default.
