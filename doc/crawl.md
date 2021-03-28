# Crawling Maven artifact versions

The public Maven central repository search API is used to retrive the latest
versions for an artifact.

This can be replaced in the future, if the volume of requests is unreasonable,
with a similar search engine replicated locally using the
[Central Maven Index](https://maven.apache.org/repository/central-index.html).
This would make discovery of new versions a bit more delayed, but it isn't the
goal of `mvnmon` to notify users _as soon as possible_. For example, many bugfix
releases come swiftly following major updates -- we would rather recommend
`2.0.1` than `2.0.0`.

### Example

```sh
# for "com.example:my-app:1.0-SNAPSHOT"
GROUP_ID="com.example"
ARTIFACT_ID="my-app"
ROWS="10"

curl "https://search.maven.org/solrsearch/select?q=g:$GROUP_ID+AND+a:$ARTIFACT_ID&start=0&rows=$ROWS"
```
