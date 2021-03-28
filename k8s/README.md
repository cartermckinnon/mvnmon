# Kubernetes manifests

The resources are sorted into two namespaces, `mvnmon-persistent` and
`mvnmon-transient`. The persistent resources hold `mvnmon`'s data, and the
transient resources are stateless. Only the transient resources need to be
re-created during an upgrade.

To deploy `mvnmon` on Kubernetes:

1. Craft your `Secret`-s. Encode them as base64 and update the `Secret` object
   in each of the two manifest files.

2. `kubectl apply -f persistent.yaml`

3. `kubectl apply -f transient.yaml`
