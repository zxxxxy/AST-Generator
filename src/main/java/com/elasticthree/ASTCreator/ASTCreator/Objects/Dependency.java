package com.elasticthree.ASTCreator.ASTCreator.Objects;

import java.util.List;

public class Dependency {
    private String groupId;

    private String artifactId;

    private String version;

    private String type;

    private String classifier;

    private String scope;

    private String systemPath;

//    private List<Exclusion> exclusions;

    private String optional;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

//    public List<Exclusion> getExclusions() {
//        return exclusions;
//    }

//    public void setExclusions(List<Exclusion> exclusions) {
//        this.exclusions = exclusions;
//    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSystemPath() {
        return systemPath;
    }

    public void setSystemPath(String systemPath) {
        this.systemPath = systemPath;
    }

    public String getOptional() {
        return optional;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }
}
