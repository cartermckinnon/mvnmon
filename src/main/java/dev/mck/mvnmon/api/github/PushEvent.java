package dev.mck.mvnmon.api.github;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents (selective fields of) a GitHub webhook "push" event.
 *
 * <p>As of March 13, 2021, this event looked like:
 *
 * <pre>
 * {
 *   "ref": "refs/heads/main",
 *   "before": "c82bfb9d64882f799cef9baba63a162a10c5a640",
 *   "after": "76ff14424d89c1a788ead25ce20c0fbbff1b82bc",
 *   "repository": {
 *     "id": 346615035,
 *     "node_id": "MDEwOlJlcG9zaXRvcnkzNDY2MTUwMzU=",
 *     "name": "test",
 *     "full_name": "mvnmon/test",
 *     "private": false,
 *     "owner": {
 *       "name": "mvnmon",
 *       "email": "80447704+mvnmon@users.noreply.github.com",
 *       "login": "mvnmon",
 *       "id": 80447704,
 *       "node_id": "MDQ6VXNlcjgwNDQ3NzA0",
 *       "avatar_url": "https://avatars.githubusercontent.com/u/80447704?v=4",
 *       "gravatar_id": "",
 *       "url": "https://api.github.com/users/mvnmon",
 *       "html_url": "https://github.com/mvnmon",
 *       "followers_url": "https://api.github.com/users/mvnmon/followers",
 *       "following_url": "https://api.github.com/users/mvnmon/following{/other_user}",
 *       "gists_url": "https://api.github.com/users/mvnmon/gists{/gist_id}",
 *       "starred_url": "https://api.github.com/users/mvnmon/starred{/owner}{/repo}",
 *       "subscriptions_url": "https://api.github.com/users/mvnmon/subscriptions",
 *       "organizations_url": "https://api.github.com/users/mvnmon/orgs",
 *       "repos_url": "https://api.github.com/users/mvnmon/repos",
 *       "events_url": "https://api.github.com/users/mvnmon/events{/privacy}",
 *       "received_events_url": "https://api.github.com/users/mvnmon/received_events",
 *       "type": "User",
 *       "site_admin": false
 *     },
 *     "html_url": "https://github.com/mvnmon/test",
 *     "description": null,
 *     "fork": false,
 *     "url": "https://github.com/mvnmon/test",
 *     "forks_url": "https://api.github.com/repos/mvnmon/test/forks",
 *     "keys_url": "https://api.github.com/repos/mvnmon/test/keys{/key_id}",
 *     "collaborators_url": "https://api.github.com/repos/mvnmon/test/collaborators{/collaborator}",
 *     "teams_url": "https://api.github.com/repos/mvnmon/test/teams",
 *     "hooks_url": "https://api.github.com/repos/mvnmon/test/hooks",
 *     "issue_events_url": "https://api.github.com/repos/mvnmon/test/issues/events{/number}",
 *     "events_url": "https://api.github.com/repos/mvnmon/test/events",
 *     "assignees_url": "https://api.github.com/repos/mvnmon/test/assignees{/user}",
 *     "branches_url": "https://api.github.com/repos/mvnmon/test/branches{/branch}",
 *     "tags_url": "https://api.github.com/repos/mvnmon/test/tags",
 *     "blobs_url": "https://api.github.com/repos/mvnmon/test/git/blobs{/sha}",
 *     "git_tags_url": "https://api.github.com/repos/mvnmon/test/git/tags{/sha}",
 *     "git_refs_url": "https://api.github.com/repos/mvnmon/test/git/refs{/sha}",
 *     "trees_url": "https://api.github.com/repos/mvnmon/test/git/trees{/sha}",
 *     "statuses_url": "https://api.github.com/repos/mvnmon/test/statuses/{sha}",
 *     "languages_url": "https://api.github.com/repos/mvnmon/test/languages",
 *     "stargazers_url": "https://api.github.com/repos/mvnmon/test/stargazers",
 *     "contributors_url": "https://api.github.com/repos/mvnmon/test/contributors",
 *     "subscribers_url": "https://api.github.com/repos/mvnmon/test/subscribers",
 *     "subscription_url": "https://api.github.com/repos/mvnmon/test/subscription",
 *     "commits_url": "https://api.github.com/repos/mvnmon/test/commits{/sha}",
 *     "git_commits_url": "https://api.github.com/repos/mvnmon/test/git/commits{/sha}",
 *     "comments_url": "https://api.github.com/repos/mvnmon/test/comments{/number}",
 *     "issue_comment_url": "https://api.github.com/repos/mvnmon/test/issues/comments{/number}",
 *     "contents_url": "https://api.github.com/repos/mvnmon/test/contents/{+path}",
 *     "compare_url": "https://api.github.com/repos/mvnmon/test/compare/{base}...{head}",
 *     "merges_url": "https://api.github.com/repos/mvnmon/test/merges",
 *     "archive_url": "https://api.github.com/repos/mvnmon/test/{archive_format}{/ref}",
 *     "downloads_url": "https://api.github.com/repos/mvnmon/test/downloads",
 *     "issues_url": "https://api.github.com/repos/mvnmon/test/issues{/number}",
 *     "pulls_url": "https://api.github.com/repos/mvnmon/test/pulls{/number}",
 *     "milestones_url": "https://api.github.com/repos/mvnmon/test/milestones{/number}",
 *     "notifications_url": "https://api.github.com/repos/mvnmon/test/notifications{?since,all,participating}",
 *     "labels_url": "https://api.github.com/repos/mvnmon/test/labels{/name}",
 *     "releases_url": "https://api.github.com/repos/mvnmon/test/releases{/id}",
 *     "deployments_url": "https://api.github.com/repos/mvnmon/test/deployments",
 *     "created_at": 1615447721,
 *     "updated_at": "2021-03-14T03:42:25Z",
 *     "pushed_at": 1615693945,
 *     "git_url": "git://github.com/mvnmon/test.git",
 *     "ssh_url": "git@github.com:mvnmon/test.git",
 *     "clone_url": "https://github.com/mvnmon/test.git",
 *     "svn_url": "https://github.com/mvnmon/test",
 *     "homepage": null,
 *     "size": 8,
 *     "stargazers_count": 0,
 *     "watchers_count": 0,
 *     "language": null,
 *     "has_issues": true,
 *     "has_projects": true,
 *     "has_downloads": true,
 *     "has_wiki": true,
 *     "has_pages": false,
 *     "forks_count": 0,
 *     "mirror_url": null,
 *     "archived": false,
 *     "disabled": false,
 *     "open_issues_count": 0,
 *     "license": null,
 *     "forks": 0,
 *     "open_issues": 0,
 *     "watchers": 0,
 *     "default_branch": "main",
 *     "stargazers": 0,
 *     "master_branch": "main"
 *   },
 *   "pusher": {
 *     "name": "mvnmon",
 *     "email": "80447704+mvnmon@users.noreply.github.com"
 *   },
 *   "sender": {
 *     "login": "mvnmon",
 *     "id": 80447704,
 *     "node_id": "MDQ6VXNlcjgwNDQ3NzA0",
 *     "avatar_url": "https://avatars.githubusercontent.com/u/80447704?v=4",
 *     "gravatar_id": "",
 *     "url": "https://api.github.com/users/mvnmon",
 *     "html_url": "https://github.com/mvnmon",
 *     "followers_url": "https://api.github.com/users/mvnmon/followers",
 *     "following_url": "https://api.github.com/users/mvnmon/following{/other_user}",
 *     "gists_url": "https://api.github.com/users/mvnmon/gists{/gist_id}",
 *     "starred_url": "https://api.github.com/users/mvnmon/starred{/owner}{/repo}",
 *     "subscriptions_url": "https://api.github.com/users/mvnmon/subscriptions",
 *     "organizations_url": "https://api.github.com/users/mvnmon/orgs",
 *     "repos_url": "https://api.github.com/users/mvnmon/repos",
 *     "events_url": "https://api.github.com/users/mvnmon/events{/privacy}",
 *     "received_events_url": "https://api.github.com/users/mvnmon/received_events",
 *     "type": "User",
 *     "site_admin": false
 *   },
 *   "created": false,
 *   "deleted": false,
 *   "forced": false,
 *   "base_ref": null,
 *   "compare": "https://github.com/mvnmon/test/compare/c82bfb9d6488...76ff14424d89",
 *   "commits": [
 *     {
 *       "id": "76ff14424d89c1a788ead25ce20c0fbbff1b82bc",
 *       "tree_id": "78d662c33a157cd0012d301f8b9a11f968917457",
 *       "distinct": true,
 *       "message": "Update pom.xml",
 *       "timestamp": "2021-03-13T19:52:25-08:00",
 *       "url": "https://github.com/mvnmon/test/commit/76ff14424d89c1a788ead25ce20c0fbbff1b82bc",
 *       "author": {
 *         "name": "mvnmon",
 *         "email": "80447704+mvnmon@users.noreply.github.com",
 *         "username": "mvnmon"
 *       },
 *       "committer": {
 *         "name": "GitHub",
 *         "email": "noreply@github.com",
 *         "username": "web-flow"
 *       },
 *       "added": [],
 *       "removed": [],
 *       "modified": [
 *         "pom.xml"
 *       ]
 *     }
 *   ],
 *   "head_commit": {
 *     "id": "76ff14424d89c1a788ead25ce20c0fbbff1b82bc",
 *     "tree_id": "78d662c33a157cd0012d301f8b9a11f968917457",
 *     "distinct": true,
 *     "message": "Update pom.xml",
 *     "timestamp": "2021-03-13T19:52:25-08:00",
 *     "url": "https://github.com/mvnmon/test/commit/76ff14424d89c1a788ead25ce20c0fbbff1b82bc",
 *     "author": {
 *       "name": "mvnmon",
 *       "email": "80447704+mvnmon@users.noreply.github.com",
 *       "username": "mvnmon"
 *     },
 *     "committer": {
 *       "name": "GitHub",
 *       "email": "noreply@github.com",
 *       "username": "web-flow"
 *     },
 *     "added": [],
 *     "removed": [],
 *     "modified": [
 *       "pom.xml"
 *     ]
 *   }
 * }
 * </pre>
 *
 * @author carter
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record PushEvent(
        @JsonProperty("ref") String ref,
        @JsonProperty("repository") Repository repository,
        @JsonProperty("commits") List<Commit> commits) {

  /** @return true if this push event was to the repository's default branch; false otherwise. */
  @JsonIgnore
  public boolean isToDefaultBranch() {
    return ref.endsWith(repository.defaultBranch());
  }

  @JsonIgnore
  public boolean containsAddedOrModifiedPoms() {
    for (Commit commit : commits) {
      if (commit.containsAddedOrModifiedPoms()) {
        return true;
      }
    }
    return false;
  }

  @JsonIgnore
  public Set<String> getPomPaths() {
    Set<String> poms = new HashSet<>();
    for (Commit commit : commits) {
      for (String addedFile : commit.getAdded()) {
        if (addedFile.endsWith("pom.xml")) {
          poms.add(addedFile);
        }
      }
      for (String modifiedFile : commit.getModified()) {
        if (modifiedFile.endsWith("pom.xml")) {
          poms.add(modifiedFile);
        }
      }
    }
    return poms;
  }
}
