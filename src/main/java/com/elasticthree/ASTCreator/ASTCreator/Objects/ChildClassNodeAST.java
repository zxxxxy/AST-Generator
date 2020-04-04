package com.elasticthree.ASTCreator.ASTCreator.Objects;

public class ChildClassNodeAST {
    private String name;

    public ChildClassNodeAST(String name){
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        String to_string = "[ \'ChildClassNodeAST - Name: " + name
                + "\']";

        return to_string;
    }
}
