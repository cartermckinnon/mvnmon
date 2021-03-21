# How Maven versions are checked

The public Maven central repository search API is used to retrive the latest
versions for an artifact. All artifacts consumed by the known repositories are checked for updates regularly (e.g. every 24 hours). This process must be orchestrated by the user (For example, with `cron` or a Kubernetes `CronJob`).

This can be replaced in the future, if the volume of requests is unreasonable, with a similar search engine populated by the [Central Maven Index](https://maven.apache.org/repository/central-index.html). This index is updated completely once a week, and incrementally updated 0 to `n` times throughout the week. This would make discovery of new versions a bit more delayed, but it isn't the goal of `mvnmon` to notify users *as soon as possible*. For example, many bugfix releases come swiftly following major updates -- we would rather recommend `2.0.1` than `2.0.0`.

### Example

```sh
# for "com.example:my-app:1.0-SNAPSHOT"
GROUP="com.example"
ARTIFACT="my-app"
ROWS="10"
curl "https://search.maven.org/solrsearch/select?q=g:$GROUP+AND+a:$ARTIFACT&start=0&rows=$ROWS"
```
