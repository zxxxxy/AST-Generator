package com.elasticthree.ASTCreator.ASTCreator.Objects;

import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.lang.reflect.Modifier;
import java.util.Set;

public class ClassNodeAST {

	private String repoURL;
	private String name;
	private String packageName;
	private String extendsClass;
	private long numberOfMethods;
	private boolean hasFinalModifier;
	private boolean hasAbstractModifier;
	private boolean hasPrivateModifier;
	private boolean hasPublicModifier;
	private boolean hasProtectedModifier;
	private boolean hasStaticModifier;
	private boolean hasSynchronizeModifier;
	private List<AnnotationNodeAST> annotations;
	private List<CommentsNodeAST> comments;
	private List<ClassImplementsNodeAST> impl;
	private List<ClassHasMethodNodeAST> method;
	private List<FieldDeclaration> classChildClass;
	private List<ParameterMethodNodeAST> methodParameters;
	private List<String> methodPartParameters;

	public ClassNodeAST(String repoURL, String name, String packageName) {
		setRepoURL(repoURL);
		setName(name);
		setPackageName(packageName);
		setExtendsClass("None");
		hasFinalModifier = false;
		hasAbstractModifier = false;
		hasPrivateModifier = false;
		hasPublicModifier = false;
		hasProtectedModifier = false;
		hasStaticModifier = false;
		hasSynchronizeModifier = false;
		numberOfMethods = 0;
		annotations = new ArrayList<AnnotationNodeAST>();
		comments = new ArrayList<CommentsNodeAST>();
		impl = new ArrayList<ClassImplementsNodeAST>();
		method = new ArrayList<ClassHasMethodNodeAST>();
		classChildClass = new ArrayList<FieldDeclaration>();
		methodParameters = new ArrayList<ParameterMethodNodeAST>();
		methodPartParameters= new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getExtendsClass() {
//		Set set = new HashSet();
//		set.addAll(extendsClass);
//		extendsClass.clear();
//		extendsClass.addAll(set);
		return extendsClass;
	}
	public void setMethodPartParameters(List<String> methodPartParameters){
		for(int i=0;i<methodPartParameters.size();i++)
		{
			this.methodPartParameters.add(methodPartParameters.get(i));
		}

	}

	public List<String> getMethodPartParameters(){
		return this.methodPartParameters;
	}
	public List<String> getDependencyClasses(){
		List<String> dependencyClasses = new ArrayList<String>();

		//类的成员变量
		for(int i=0;i<this.classChildClass.size();i++)
		{

			Type t=this.classChildClass.get(i).getType();
			dependencyClasses.add(t.toString());
		}

		//方法的参数
		if(this.getMethodParameters()!=null)
		{
			for(int i=0;i<this.getMethodParameters().size();i++)
			{

				dependencyClasses.add(this.getMethodParameters().get(i).getType().toString());
			}
		}


		//方法的局部变量
		// 这个getMethodPartParameters好像是拿的和MethodParameter还是啥是重复的 实际上没有拿到方法的局部变量
		if(this.getMethodPartParameters()!=null)
		{
			for(int i=0;i<this.getMethodPartParameters().size();i++)
			{
				dependencyClasses.add(getMethodPartParameters().get(i));
			}
		}
		// 这里才是方法的局部变量
		if(this.getMethod()!=null)
		{
			for(int i=0;i<this.getMethod().size();i++)
			{
				dependencyClasses.addAll(getMethod().get(i).getMethodChildClass());
			}
		}

		// 去重
		Set set = new HashSet();
		set.addAll(dependencyClasses);
		dependencyClasses.clear();
		dependencyClasses.addAll(set);

		return dependencyClasses;
	}

	public void setExtendsClass(String extendsClass) {
		this.extendsClass = extendsClass;
	}

	public long getNumberOfMethods() {
		return numberOfMethods;
	}

	public void setNumberOfMethods(long numberOfMethods) {
		this.numberOfMethods = numberOfMethods;
	}

	public boolean isHasFinalModifier() {
		return hasFinalModifier;
	}

	public void setHasFinalModifier(boolean hasFinalModifier) {
		this.hasFinalModifier = hasFinalModifier;
	}

	public boolean isHasAbstractModifier() {
		return hasAbstractModifier;
	}

	public void setHasAbstractModifier(boolean hasAbstractModifier) {
		this.hasAbstractModifier = hasAbstractModifier;
	}

	public boolean isHasPrivateModifier() {
		return hasPrivateModifier;
	}

	public void setHasPrivateModifier(boolean hasPrivateModifier) {
		this.hasPrivateModifier = hasPrivateModifier;
	}

	public boolean isHasPublicModifier() {
		return hasPublicModifier;
	}

	public void setHasPublicModifier(boolean hasPublicModifier) {
		this.hasPublicModifier = hasPublicModifier;
	}

	public boolean isHasProtectedModifier() {
		return hasProtectedModifier;
	}

	public void setHasProtectedModifier(boolean hasProtectedModifier) {
		this.hasProtectedModifier = hasProtectedModifier;
	}

	public boolean isHasStaticModifier() {
		return hasStaticModifier;
	}

	public void setHasStaticModifier(boolean hasStaticModifier) {
		this.hasStaticModifier = hasStaticModifier;
	}

	public boolean isHasSynchronizeModifier() {
		return hasSynchronizeModifier;
	}

	public void setHasSynchronizeModifier(boolean hasSynchronizeModifier) {
		this.hasSynchronizeModifier = hasSynchronizeModifier;
	}

	public List<AnnotationNodeAST> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<AnnotationNodeAST> annotations) {
		this.annotations = annotations;
	}

	public List<CommentsNodeAST> getComments() {
		return comments;
	}

	public void setComments(List<CommentsNodeAST> comments) {
		this.comments = comments;
	}

	public List<ClassImplementsNodeAST> getImpl() {
		return impl;
	}

	public void setChildClasses(List<FieldDeclaration> childClass){
		this.classChildClass = childClass;


	}
	public List<FieldDeclaration> getChildClasses(){
		return classChildClass;

	}

	public void setMethodParameters(List<ParameterMethodNodeAST> methodParameters){
		this.methodParameters = methodParameters;

	}
	public List<ParameterMethodNodeAST> getMethodParameters(){
		return methodParameters;

	}

	public void setImpl(List<ClassImplementsNodeAST> impl) {
		this.impl = impl;
	}

	public List<ClassHasMethodNodeAST> getMethod() {
		return method;
	}

	public void setMethod(List<ClassHasMethodNodeAST> method) {
		this.method = method;
	}

	public void setAllModifiers(int mod){
		if (Modifier.isFinal(mod)) {
			hasFinalModifier = true;
		}
		if (Modifier.isAbstract(mod)){
			hasAbstractModifier = true;
		}
		if (Modifier.isPrivate(mod)){
			hasPrivateModifier = true;
		}
		if (Modifier.isPublic(mod)){
			hasPublicModifier = true;
		}
		if (Modifier.isProtected(mod)){
			hasProtectedModifier = true;
		}
		if (Modifier.isStatic(mod)){
			hasStaticModifier = true;
		}
		if (Modifier.isSynchronized(mod)){
			hasStaticModifier = true;
		}
	}


	@Override
	public String toString(){
		String to_string = "[ \'repoURL: " + repoURL + "\', \'Package : " + packageName + "\', \'Name: "
				+ name
				+ "\', \'ExtendsClass: " + extendsClass
				+ "\', \'NumberOfMethods : " + numberOfMethods
				+ "\', \'HasFinalModifier : " + hasFinalModifier
				+ "\', \'HasAbstractModifier : " + hasAbstractModifier
				+ "\', \'HasPrivateModifier : " + hasPrivateModifier
				+ "\', \'HasPublicModifier : " + hasPublicModifier
				+ "\', \'HasProtectedModifier : " + hasProtectedModifier
				+ "\', \'HasStaticModifier : " + hasStaticModifier
				+ "\', \'HasSynchronizeModifier : " + hasSynchronizeModifier
				+ "\']";
		if (annotations.size() != 0 )
			for(int i = 0; i< annotations.size(); i++)
				to_string += "\n" + annotations.get(i).toString();

		if (comments.size() != 0 )
			for(int i=0; i<comments.size(); i++)
				to_string += "\n" + comments.get(i).toString();

		if (impl.size() != 0 )
			for(int i=0; i<impl.size(); i++)
				to_string += "\n" + impl.get(i).toString();

		if (method.size() != 0 ){
			for(int i=0; i<method.size(); i++)
				to_string += "\n" + method.get(i).toString();
		}

		return to_string;
	}

	public String getRepoURL() {
		return repoURL;
	}

	public void setRepoURL(String repoURL) {
		this.repoURL = repoURL;
	}

}
