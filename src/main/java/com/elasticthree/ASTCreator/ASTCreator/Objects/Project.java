package com.elasticthree.ASTCreator.ASTCreator.Objects;

import java.util.List;

public class Project {

    private String pomPath;//eg. C:\Users\zxy\Desktop\项目\microservices-platform-master\microservices-platform-master\zlt-commons\pom.xml

    private String groupId;

    private String artifactId;

    private String version;

    private Project parent;

    private List<Dependency> dependencies;

    private List<Dependency> dependencyManagement;

    private String build;

    private List<String> modules;

//    public void print(){
//        //仅供调试粗略使用
//        System.out.println("---entity.Project ");
//        System.out.println("groupId: " + getGroupId());
//        System.out.println("artifactId: " + getArtifactId());
//        System.out.println("version: " + getVersion());
//        System.out.println("parent: " + getParent().getGroupId() + "/" + getParent().getArtifactId() + "/" + getParent().getVersion());
//        System.out.println("dependencies: --- start");
//        for(int i = 0; i < dependencies.size(); i++){
//            System.out.println("[" + i + "]");
//            System.out.println(" -groupId: " + dependencies.get(i).getGroupId());
//            System.out.println(" -artifactId: " + dependencies.get(i).getArtifactId());
//            System.out.println(" -version: " + dependencies.get(i).getVersion());
////            if(dependencies.get(i).getExclusions().size() > 0){
////                System.out.println(" -exclusions: --- start");
////                for(int j = 0; j < dependencies.get(i).getExclusions().size(); j++){
////                    System.out.println("[" + j + "]");
////                    System.out.println("  --groupId: " + dependencies.get(i).getExclusions().get(j).getGroupId());
////                    System.out.println("  --artifactId: " + dependencies.get(i).getExclusions().get(j).getArtifactId());
////                    System.out.println("  --version: " + dependencies.get(i).getExclusions().get(j).getVersion());
////                }
////                System.out.println(" -exclusions: --- end");
////            }
//        }
//        System.out.println("dependencies: --- end");
//        System.out.println("dependencyManagement: --- start");
//        for(int i = 0; i < dependencyManagement.size(); i++){
//            System.out.println("[" + i + "]");
//            System.out.println(" -groupId: " + dependencyManagement.get(i).getGroupId());
//            System.out.println(" -artifactId: " + dependencyManagement.get(i).getArtifactId());
//            System.out.println(" -version: " + dependencyManagement.get(i).getVersion());
////            if(dependencyManagement.get(i).getExclusions().size() > 0){
////                System.out.println(" -exclusions: --- start");
////                for(int j = 0; j < dependencyManagement.get(i).getExclusions().size(); j++){
////                    System.out.println("[" + j + "]");
////                    System.out.println("  --groupId: " + dependencyManagement.get(i).getExclusions().get(j).getGroupId());
////                    System.out.println("  --artifactId: " + dependencyManagement.get(i).getExclusions().get(j).getArtifactId());
////                    System.out.println("  --version: " + dependencyManagement.get(i).getExclusions().get(j).getVersion());
////                }
//                System.out.println(" -exclusions: --- end");
//            }
//        }
//        System.out.println("dependencyManagement: --- end");
//        System.out.println("build: ");
//        System.out.println(build);
//        System.out.println("modules: --- start");
//        for(int i = 0; i < modules.size(); i++){
//            System.out.println(" -module: " + modules.get(i));
//        }
//        System.out.println("modules: --- end");
//        System.out.println("---entity.Project end");
//    }


    public String getPomPath(){return pomPath;}

    public void setPomPath(String s){
        this.pomPath=s;
    }

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

    public Project getParent() {
        return parent;
    }

    public void setParent(Project parent) {
        this.parent = parent;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public List<Dependency> getDependencyManagement() {
        return dependencyManagement;
    }

    public void setDependencyManagement(List<Dependency> dependencyManagement) {
        this.dependencyManagement = dependencyManagement;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }
}
