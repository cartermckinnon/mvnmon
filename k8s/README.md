# Kubernetes manifests

1. Craft your `Secret`-s. Encode them as base64 and update the `Secret` object
   in each of the two manifest files.

2. Apply the `persistent.yaml` resources (NATS, Postgres).

3. Apply the `transient.yaml` resources (frontend, backend).
