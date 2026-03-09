package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RepoVersion represents git repository version information.
 * Corresponds to uniast.RepoVersion in Go
 */
public class RepoVersion {
    @JsonProperty("Branch")
    private String branch;

    @JsonProperty("Commit")
    private String commit;

    @JsonProperty("CommitTime")
    private String commitTime;

    @JsonProperty("RemoteURL")
    private String remoteURL;

    public RepoVersion() {
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(String commitTime) {
        this.commitTime = commitTime;
    }

    public String getRemoteURL() {
        return remoteURL;
    }

    public void setRemoteURL(String remoteURL) {
        this.remoteURL = remoteURL;
    }
}
