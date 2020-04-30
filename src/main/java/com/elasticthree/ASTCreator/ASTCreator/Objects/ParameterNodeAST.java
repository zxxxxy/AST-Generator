package com.elasticthree.ASTCreator.ASTCreator.Objects;

import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.List;

/**
 * Created by yangchen on 4/14/20.
 */
public class ParameterNodeAST {

    private String type;
    private String name;
    private String target;
    private List<AnnotationExpr> annotations;

    public ParameterNodeAST(String type, String name, String target, List<AnnotationExpr> annotations) {
        this.setType(type);
        this.setName(name);
        this.setTarget(target);
        this.setAnnotations(annotations);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<AnnotationExpr> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationExpr> annotations) {
        this.annotations = annotations;
    }
}
