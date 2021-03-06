# How Maven versions are checked

The public Maven central repository search API is used to retrive the latest
version(s) for an artifact. If the version\* is newer than the last observed
version, an alert is fired.

### Example

```sh
# for "com.example:my-app:1.0-SNAPSHOT"
GROUP="com.example"
ARTIFACT="my-app"
ROWS="10"
curl "https://search.maven.org/solrsearch/select?q=g:$GROUP+AND+a:$ARTIFACT&start=0&rows=$ROWS"
```
