package com.elasticthree.ASTCreator.ASTCreator.Objects;

import com.github.javaparser.ast.expr.MemberValuePair;

import java.util.List;

public class AnnotationNodeAST {

	private String name;
	private List<MemberValuePair> annotationParameters;
	
	public AnnotationNodeAST(String name,List<MemberValuePair> parameter){
		this.setName(name);
		this.setAnnotationParameters(parameter);
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public List<MemberValuePair> getAnnotationParameters(){
		return annotationParameters;
	}
	public void setAnnotationParameters(List<MemberValuePair> parameters){
		this.annotationParameters = parameters;
	}
	
	@Override
	public String toString(){
		String to_string = "[ \'AnnotationNodeAST - Name: " + name
				+ "\']";
		
		return to_string;
	}
	
}
