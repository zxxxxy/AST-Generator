package com.elasticthree.ASTCreator.ASTCreator.Neo4jDriver;

import com.elasticthree.ASTCreator.ASTCreator.Helpers.StaticVariables;
import com.elasticthree.ASTCreator.ASTCreator.Objects.*;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import org.apache.log4j.Logger;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Neo4JDriver {

	final static Logger logger = Logger.getLogger(Neo4JDriver.class);
	final static Logger debugLog = Logger.getLogger("debugLogger");
	final static Logger resultLog = Logger.getLogger("reportsLogger");

	private String host;
	private String usern;
	private String password;
	private Driver driver;
	private Session session;

	public static Set<String> ClassList = new HashSet<String>();

	/**
	 * Neo4JDriver creates and inserts the query to Neo4j instance
	 */
	public Neo4JDriver() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			String projectPath = System.getProperty("user.dir");
			input = new FileInputStream(projectPath + "/src/main/resources/config.properties");
			prop.load(input);
			this.host = prop.getProperty("host");
			this.usern = prop.getProperty("neo4j_username");
			this.password = prop.getProperty("neo4j_password");
		} catch (IOException ex) {
			logger.debug("IOException: ", ex);
			debugLog.debug("IOException: ", ex);
			host = null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			driver = GraphDatabase.driver("bolt://" + this.host,
					AuthTokens.basic(this.usern, this.password));
			session = driver.session();
		} catch (Exception e) {
			driver = null;
			session = null;
		}

	}

	/**
	 * Inserting AST to Neo4J instance through log file
	 *
	 * @param query
	 */
	public void insertNeo4JDBLogFile(String query) {

		if (isNeo4jConnectionUp()) {

			try {
				// Insert query on Neo4j graph DB
				session.run(query);
				logger.info("Insertion Query: " + query);

			} catch (Exception e) {
				logger.debug("Excetion : ", e);
				debugLog.debug("Excetion : ", e);
				return;
			}
		} else {
			logger.debug("Driver or Session is down, check the configuration");
			debugLog.debug("Driver or Session is down, check the configuration");
		}

	}

	/**
	 * Inserting AST to Neo4J instance
	 *
	 * @param fileNodeAST
	 *            (AST root node)
	 */
	public void insertNeo4JDB(FileNodeAST fileNodeAST) {

		if (fileNodeAST == null) {
			logger.debug("AST File Object is null (Probably had parsing error)");
			debugLog.debug("AST File Object is null (Probably had parsing error)");
			return;
		}

		if (isNeo4jConnectionUp()) {
			try {
				// File Node of AST
				String fileNodeInsertQuery = "CREATE (";
				fileNodeInsertQuery += "f:" + StaticVariables.fileNodeName
						+ " {";
				// File node properties
				fileNodeInsertQuery += StaticVariables.URLRepoPropertyName
						+ ":\'" + fileNodeAST.getRepoURL() + "\',";
				fileNodeInsertQuery += StaticVariables.packagePropertyName
						+ ":\'" + fileNodeAST.getPackageName() + "\',";
				fileNodeInsertQuery += StaticVariables.fileNodeArtifactId
						+ ":\'" + fileNodeAST.getArtifactId() + "\',";
				fileNodeInsertQuery += StaticVariables.namePropertyName + ":\'"
						+ fileNodeAST.getName() + "\',";
				fileNodeInsertQuery += StaticVariables.numberOfClassesPropertyName
						+ ":"
						+ String.valueOf(fileNodeAST.getNumberOfClasses())
						+ ",";
				fileNodeInsertQuery += StaticVariables.numberOfInterfacesPropertyName
						+ ":"
						+ String.valueOf(fileNodeAST.getNumberOfInterfaces())
						+ ""
						+ "})";

				// imports nodes
				if (fileNodeAST.getImports().size() > 0) {
					List<ImportDeclaration> imports = fileNodeAST.getImports();
					for (int im=0; im<imports.size(); im ++) {
						String[] filenames = fileNodeAST.getName().split("\\/|\\.");
						String fileName = filenames[filenames.length-2];
						fileNodeInsertQuery += ",(";
						fileNodeInsertQuery +=
								"imports" + fileNodeAST.getPackageName().replace(".", "T") + "T" + fileName
										+ String.valueOf(im)
										+ ":" + StaticVariables.importNodeName + " {";
						// import node properties
						fileNodeInsertQuery += StaticVariables.nameParameterPropertyName
								+ ":\'"
								+ imports.get(im).getName().toString()
								+ "\'";
						fileNodeInsertQuery += "})";

						// RELATION SHIP FILE -> Import
						fileNodeInsertQuery += ",(" + "f" + ")";

						fileNodeInsertQuery += "-[:"
								+ StaticVariables.has_importPropertyName
								+ "]->";
						fileNodeInsertQuery += "("
								+ "imports" + fileNodeAST.getPackageName().replace(".", "T") + "T" + fileName
								+ String.valueOf(im) + ")";
					}
				}

				// List of Classes
				if (fileNodeAST.getNumberOfClasses() > 0) {

					for (int i = 0; i < fileNodeAST.getClasses().size(); i++) {
						ClassNodeAST classNode = fileNodeAST.getClasses()
								.get(i);

						ClassList.add(classNode.getName());

						fileNodeInsertQuery += ",(";
						fileNodeInsertQuery +=
								"class"
										+ classNode.getName()
										+ String.valueOf(i)
										+ ":" + StaticVariables.classNodeName + " {";
						// Class node properties
						if (classNode.isHasFinalModifier())
							fileNodeInsertQuery += "hasFinalModifier:\'"
									+ String.valueOf(classNode
									.isHasFinalModifier()) + "\',";
						if (classNode.isHasAbstractModifier())
							fileNodeInsertQuery += "hasAbstractModifier:\'"
									+ String.valueOf(classNode
									.isHasAbstractModifier()) + "\',";
						if (classNode.isHasPrivateModifier())
							fileNodeInsertQuery += "hasPrivateModifier:\'"
									+ String.valueOf(classNode
									.isHasPrivateModifier()) + "\',";
						if (classNode.isHasPublicModifier())
							fileNodeInsertQuery += "hasPublicModifier:\'"
									+ String.valueOf(classNode
									.isHasPublicModifier()) + "\',";
						if (classNode.isHasProtectedModifier())
							fileNodeInsertQuery += "hasProtectedModifier:\'"
									+ String.valueOf(classNode
									.isHasProtectedModifier()) + "\',";
						if (classNode.isHasStaticModifier())
							fileNodeInsertQuery += "hasStaticModifier:\'"
									+ String.valueOf(classNode
									.isHasStaticModifier()) + "\',";
						if (classNode.isHasSynchronizeModifier())
							fileNodeInsertQuery += "hasSynchronizeModifier:\'"
									+ String.valueOf(classNode
									.isHasSynchronizeModifier())
									+ "\',";
						if (!classNode.getExtendsClass().equalsIgnoreCase(
								"None")) {
							fileNodeInsertQuery += StaticVariables.extendsClassPropertyName
									+ ":\'"
									+ classNode.getExtendsClass()
									+ "\',";

						}



						fileNodeInsertQuery += StaticVariables.numberOfMethodsClassPropertyName
								+ ":"
								+ String.valueOf(classNode.getNumberOfMethods())
								+ ",";
						fileNodeInsertQuery += StaticVariables.URLRepoPropertyName
								+ ":\'" + classNode.getRepoURL() + "\',";
						fileNodeInsertQuery += StaticVariables.packageClassPropertyName
								+ ":\'" + classNode.getPackageName() + "\',";
						fileNodeInsertQuery += StaticVariables.nameClassPropertyName
								+ ":\'" + classNode.getName() + "\'";
						fileNodeInsertQuery += "})";

						// Class Level Annotation Node
						if (classNode.getAnnotations().size() > 0) {
							for (int j = 0; j < classNode.getAnnotations()
									.size(); j++) {
								AnnotationNodeAST annotationNode = classNode
										.getAnnotations().get(j);
								fileNodeInsertQuery += ",(";
								fileNodeInsertQuery +=
										"class"
												+ classNode.getName()
												+ String.valueOf(i)
												+ "ann"
												+ String.valueOf(j) + ":"
												+ StaticVariables.annotationNodeName
												+ " {";

								// Annotation node property
								fileNodeInsertQuery += StaticVariables.nameAnnotationPropertyName
										+ ":\'"
										+ annotationNode.getName().replace("\'", "")//.replace("\"", "|")    //将双引号变成|
										+ "\',";
								fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
										+ ":\'"
										+ "CLASS"
										+ "\'";

								//annotation add parameters as property
								if(annotationNode.getAnnotationParameters().size()!=0)
								{
									fileNodeInsertQuery += ","
											+ StaticVariables.annotationParameterName
											+ ":'[";
									for(int count=0;count<annotationNode.getAnnotationParameters().size();count++)
									{
										fileNodeInsertQuery += "{"
												+ StaticVariables.annotationParameterListKeyWord
												+":"
												+"\""
												+ annotationNode.getAnnotationParameters().get(count).getName()
												+ "\""
												+ ","
												+ StaticVariables.annotationParameterListValueWord
												+ ":"
//														+ "\""
												+ annotationNode.getAnnotationParameters().get(count).getValue().toString()
//														+ "\""
												+ "}";
										if(count!=annotationNode.getAnnotationParameters().size()-1)
										{
											fileNodeInsertQuery+=",";
										}

									}

									fileNodeInsertQuery += "]'";

								}

								fileNodeInsertQuery += "})";

								// RELATION SHIP CLASS -> ANNOTATION
								fileNodeInsertQuery += ",("
										+ "class"
										+ classNode.getName()
										+ String.valueOf(i)
										+ ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.has_annotationPropertyName
										+ "]->";
								fileNodeInsertQuery += "("
										+ "class"
										+ classNode.getName()
										+ String.valueOf(i)
										+ "ann"
										+ String.valueOf(j) + ")";

								// RELATION SHIP FILE -> Annotation
								fileNodeInsertQuery += ",(" + "f" + ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.has_annotationPropertyName
										+ "]->";
								fileNodeInsertQuery += "("
										+ "class"
										+ classNode.getName()
										+ String.valueOf(i)
										+ "ann"
										+ String.valueOf(j) + ")";
							}
						}

						// all parameter level annotation node ; from class to parameter
						List<FieldDeclaration> parametersInClass = classNode.getChildClasses();
						for (int j = 0; j < parametersInClass.size(); j ++) {
							// all parameters
							FieldDeclaration parameter = parametersInClass.get(j);
							ParameterMethodNodeAST parameterMethodNodeAST = new ParameterMethodNodeAST(
									parameter.getType().toString(),
									parameter.getVariables().get(0).getId().toString()
							);
							fileNodeInsertQuery += ",(";
							fileNodeInsertQuery +=
									"class"
											+ classNode.getName()
											+ String.valueOf(i)
											+ "FIELD"
											+ "param"
											+ parameterMethodNodeAST.getName()
											+ String.valueOf(j)
											+ ":"
											+ StaticVariables.parameterNodeName
											+ " {";
							// Parameter node property
							fileNodeInsertQuery += StaticVariables.nameParameterPropertyName
									+ ":\'"
									+ parameterMethodNodeAST.getName()
									+ "\',";
							fileNodeInsertQuery += StaticVariables.typeParameterPropertyName
									+ ":\'"
									+ parameterMethodNodeAST.getType()
									+ "\',";
							fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
									+ ":\'"
									+ "FIELD"
									+ "\'";
							fileNodeInsertQuery += "})";

							// RELATION SHIP Class -> Parameter
							fileNodeInsertQuery += ",("
									+ "class"
									+ classNode.getName()
									+ String.valueOf(i)
									+ ")";

							fileNodeInsertQuery += "-[:"
									+ StaticVariables.has_parameterPropertyName
									+ "]->";
							fileNodeInsertQuery +=
									"(class" + classNode.getName()
											+ String.valueOf(i)
											+ "FIELD"
											+ "param"
											+ parameterMethodNodeAST.getName()
											+ String.valueOf(j) + ")";

							// parameter level annotation
							List<AnnotationExpr> annotationExprs =  parameter.getAnnotations();
							for (int k = 0; k < annotationExprs.size(); k ++) {
								fileNodeInsertQuery += ",(";
								fileNodeInsertQuery +=
										"class" + classNode.getName() + String.valueOf(i) +
												"parameter" + parameterMethodNodeAST.getName()
												+ "ann"
												+ String.valueOf(j)
												+ String.valueOf(k) + ":"
												+ StaticVariables.annotationNodeName
												+ " {";

								// Annotation node property
								AnnotationExpr annotationExpr = annotationExprs.get(k);
								fileNodeInsertQuery += StaticVariables.nameAnnotationPropertyName
										+ ":\'"
										+ "@" + annotationExpr.getName().toString().replace("\'", "")//.replace("\"", "|")    //将双引号变成|
										+ "\',";
								fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
										+ ":\'"
										+ "FIELD"
										+ "\'";
								//annotation add parameters as property
								// annotation's parameters
								fileNodeInsertQuery += ","
										+ StaticVariables.annotationParameterName
										+ ":'[";
								// first one is annotation itself
								if (annotationExpr.getChildrenNodes().size() > 1) {
									for(int count=1; count < annotationExpr.getChildrenNodes().size(); count++)
									{
										MemberValuePair memberValuePair = (MemberValuePair) annotationExpr.getChildrenNodes().get(count);
										fileNodeInsertQuery += "{"
												+ StaticVariables.annotationParameterListKeyWord
												+":"
												+"\""
												+ memberValuePair.getName()
												+ "\""
												+ ","
												+ StaticVariables.annotationParameterListValueWord
												+ ":"
//														+ "\""
												+ memberValuePair.getValue().toString()
//														+ "\""
												+ "}";
										if(count != annotationExpr.getChildrenNodes().size() - 1)
										{
											fileNodeInsertQuery+=",";
										}
									}
								}
								fileNodeInsertQuery += "]'";

								fileNodeInsertQuery += "})";
								// RELATION SHIP Parameter -> ANNOTATION
								fileNodeInsertQuery += ",("
										+ "class"
										+ classNode.getName()
										+ String.valueOf(i)
										+ "FIELD"
										+ "param"
										+ parameterMethodNodeAST.getName()
										+ String.valueOf(j)
										+ ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.has_annotationPropertyName
										+ "]->";
								fileNodeInsertQuery +=
										"(class" + classNode.getName() + String.valueOf(i) +
												"parameter" + parameterMethodNodeAST.getName()
												+ "ann"
												+ String.valueOf(j)
												+ String.valueOf(k) +  ")";

								// RELATION SHIP FILE -> Annotation
								fileNodeInsertQuery += ",(" + "f" + ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.has_annotationPropertyName
										+ "]->";
								fileNodeInsertQuery += "("
										+ "class"
										+ classNode.getName() + String.valueOf(i) +
										"parameter" + parameterMethodNodeAST.getName()
										+ "ann"
										+ String.valueOf(j)
										+ String.valueOf(k) + ")";
							}

						}

						// Implements Interface Node
						if (classNode.getImpl().size() > 0) {
							for (int j = 0; j < classNode.getImpl().size(); j++) {
								ClassImplementsNodeAST implNode = classNode
										.getImpl().get(j);
								fileNodeInsertQuery += ",(";
								fileNodeInsertQuery +=
										"class"
												+ classNode.getName()
												+ String.valueOf(i)
												+ "impl"
												+ String.valueOf(j)
												+ ":"
												+ StaticVariables.implementsInterfaceNodeName
												+ " {";
								// Implements Interface node property
								fileNodeInsertQuery += StaticVariables.implementsInterfacePropertyName
										+ ":\'" + implNode.getName() + "\'";
								fileNodeInsertQuery += "})";

								// RELATION SHIP CLASS -> IMPLEMENTS_INTERFACE
								fileNodeInsertQuery += ",("
										+ "class"
										+ classNode.getName()
										+ String.valueOf(i)
										+ ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.implements_interfacePropertyName
										+ "]->";
								fileNodeInsertQuery += "("
										+ "class"
										+ classNode.getName()
										+ String.valueOf(i)
										+ "impl"
										+ String.valueOf(j) + ")";
							}
						}

						// Method Node
						if (classNode.getMethod().size() > 0) {
							for (int j = 0; j < classNode.getMethod().size(); j++) {
								ClassHasMethodNodeAST methodNode = classNode
										.getMethod().get(j);
								fileNodeInsertQuery += ",(";
								fileNodeInsertQuery +=
										"class"
												+ classNode.getName()
												+ String.valueOf(i)
												+ "method"
												+ String.valueOf(j) + ":"
												+ StaticVariables.methodNodeName + " {";

								if (methodNode.isHasFinalModifier())
									fileNodeInsertQuery += "hasFinalModifier:\'"
											+ String.valueOf(methodNode
											.isHasFinalModifier())
											+ "\',";
								if (methodNode.isHasAbstractModifier())
									fileNodeInsertQuery += "hasAbstractModifier:\'"
											+ String.valueOf(methodNode
											.isHasAbstractModifier())
											+ "\',";
								if (methodNode.isHasPrivateModifier())
									fileNodeInsertQuery += "hasPrivateModifier:\'"
											+ String.valueOf(methodNode
											.isHasPrivateModifier())
											+ "\',";
								if (methodNode.isHasPublicModifier())
									fileNodeInsertQuery += "hasPublicModifier:\'"
											+ String.valueOf(methodNode
											.isHasPublicModifier())
											+ "\',";
								if (methodNode.isHasProtectedModifier())
									fileNodeInsertQuery += "hasProtectedModifier:\'"
											+ String.valueOf(methodNode
											.isHasProtectedModifier())
											+ "\',";
								if (methodNode.isHasStaticModifier())
									fileNodeInsertQuery += "hasStaticModifier:\'"
											+ String.valueOf(methodNode
											.isHasStaticModifier())
											+ "\',";
								if (methodNode.isHasSynchronizeModifier())
									fileNodeInsertQuery += "hasSynchronizeModifier:\'"
											+ String.valueOf(methodNode
											.isHasSynchronizeModifier())
											+ "\',";

								fileNodeInsertQuery += StaticVariables.returningTypeMethodPropertyName
										+ ":\'"
										+ methodNode.getReturningType()
										+ "\',";
								fileNodeInsertQuery += StaticVariables.URLRepoPropertyName
										+ ":\'" + classNode.getRepoURL() + "\',";
								fileNodeInsertQuery += StaticVariables.packageMethodPropertyName
										+ ":\'"
										+ methodNode.getPackageName()
										+ "\',";
								fileNodeInsertQuery += StaticVariables.nameMethodPropertyName
										+ ":\'" + methodNode.getName() + "\'";
								fileNodeInsertQuery += "})";

								// Method's RelationShips

								// Method Level Annotation Node
								if (methodNode.getAnnotatios().size() > 0) {
									for (int k = 0; k < methodNode
											.getAnnotatios().size(); k++) {
										AnnotationNodeAST annotationNode = methodNode
												.getAnnotatios().get(k);
										fileNodeInsertQuery += ",(";
										fileNodeInsertQuery +=
												"class"
														+ classNode.getName()
														+ String.valueOf(i)
														+ "method"
														+ methodNode.getName()
														+ String.valueOf(j)
														+ "ann"
														+ String.valueOf(k)
														+ ":"
														+ StaticVariables.annotationNodeName
														+ " {";
										// Annotation node property
										fileNodeInsertQuery += StaticVariables.nameAnnotationPropertyName
												+ ":\'"
												+ annotationNode.getName().replace("\'", "")//.replace("\"", "")
												+ "\',";
										fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
												+ ":\'"
												+ "METHOD"
												+ "\'";

										//annotation add parameters as property
										if(annotationNode.getAnnotationParameters().size()!=0)
										{
											fileNodeInsertQuery += ","
													+ StaticVariables.annotationParameterName
													+ ":'[";
											for(int count=0;count<annotationNode.getAnnotationParameters().size();count++)
											{
												fileNodeInsertQuery += "{"
														+ StaticVariables.annotationParameterListKeyWord
														+":"
														+"\""
														+ annotationNode.getAnnotationParameters().get(count).getName()
														+ "\""
														+ ","
														+ StaticVariables.annotationParameterListValueWord
														+ ":"
//														+ "\""
														+ annotationNode.getAnnotationParameters().get(count).getValue().toString()
//														+ "\""
														+ "}";
												if(count!=annotationNode.getAnnotationParameters().size()-1)
												{
													fileNodeInsertQuery+=",";
												}

											}

											fileNodeInsertQuery += "]'";

										}

										fileNodeInsertQuery += "})";

										// RELATION SHIP METHOD -> ANNOTATION
										fileNodeInsertQuery += ",("
												+ "class"
												+ classNode.getName()
												+ String.valueOf(i)
												+ "method" + String.valueOf(j)
												+ ")";

										fileNodeInsertQuery += "-[:"
												+ StaticVariables.has_annotationPropertyName
												+ "]->";
										fileNodeInsertQuery += "("
												+ "class"
												+ classNode.getName()
												+ String.valueOf(i)
												+ "method"
												+ methodNode.getName()
												+ String.valueOf(j) + "ann"
												+ String.valueOf(k) + ")";

										// RELATION SHIP FILE -> Annotation
										fileNodeInsertQuery += ",(" + "f" + ")";

										fileNodeInsertQuery += "-[:"
												+ StaticVariables.has_annotationPropertyName
												+ "]->";
										fileNodeInsertQuery += "("
												+ "class"
												+ classNode.getName()
												+ String.valueOf(i)
												+ "method"
												+ methodNode.getName()
												+ String.valueOf(j) + "ann"
												+ String.valueOf(k) + ")";
									}
								}

								// Parameter Node
								if (methodNode.getParametersMethod().size() > 0) {
									for (int k = 0; k < methodNode.getParametersMethod().size(); k++) {
										ParameterNodeAST paramNode = methodNode.getParametersMethod().get(k);
										fileNodeInsertQuery += ",(";
										fileNodeInsertQuery +=
												"class"
														+ classNode.getName()
														+ String.valueOf(i)
														+ "method"
														+ methodNode.getName()
														+ String.valueOf(j)
														+ "param"
														+ String.valueOf(k)
														+ ":"
														+ StaticVariables.parameterNodeName
														+ " {";
										// Parameter node property
										fileNodeInsertQuery += StaticVariables.nameParameterPropertyName
												+ ":\'"
												+ paramNode.getName()
												+ "\',";
										fileNodeInsertQuery += StaticVariables.typeParameterPropertyName
												+ ":\'"
												+ paramNode.getType()
												+ "\',";
										fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
												+ ":\'"
												+ paramNode.getTarget()
												+ "\'";
										fileNodeInsertQuery += "})";

										// RELATION SHIP METHOD -> PARAMETER
										fileNodeInsertQuery += ",("
												+ "class"
												+ classNode.getName()
												+ String.valueOf(i)
												+ "method" + String.valueOf(j)
												+ ")";

										fileNodeInsertQuery += "-[:"
												+ StaticVariables.has_parameterPropertyName
												+ "]->";
										fileNodeInsertQuery += "("
												+ "class"
												+ classNode.getName()
												+ String.valueOf(i)
												+ "method"
												+ methodNode.getName()
												+ String.valueOf(j) + "param"
												+ String.valueOf(k)
												+ ")";

										// parameter level annotation
										List<AnnotationExpr> paramAnnotations = paramNode.getAnnotations();
										if (paramAnnotations.size() > 0) {
											for (int l = 0; l < paramAnnotations.size(); l ++) {
												fileNodeInsertQuery += ",(";
												fileNodeInsertQuery +=
														"class" + classNode.getName() + String.valueOf(i)
																+ "VARIABLEPARAM"
																+ "parameter" + paramNode.getName()
																+ "ann"
																+ String.valueOf(j)
																+ String.valueOf(k)
																+ String.valueOf(l) + ":"
																+ StaticVariables.annotationNodeName
																+ " {";
												// Annotation node property
												AnnotationExpr annotationExprParam = paramAnnotations.get(l);
												fileNodeInsertQuery += StaticVariables.nameAnnotationPropertyName
														+ ":\'"
														+ "@" + annotationExprParam.getName().toString().replace("\'", "")//.replace("\"", "|")    //将双引号变成|
														+ "\',";
												fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
														+ ":\'"
														+ paramNode.getTarget()
														+ "\'";
												//annotation add parameters as property
												// annotation's parameters
												fileNodeInsertQuery += ","
														+ StaticVariables.annotationParameterName
														+ ":'[";
												// first one is annotation itself
												if (annotationExprParam.getChildrenNodes().size() > 1) {
													for(int count=1; count < annotationExprParam.getChildrenNodes().size(); count++)
													{
														MemberValuePair memberValuePair = (MemberValuePair) annotationExprParam.getChildrenNodes().get(count);
														fileNodeInsertQuery += "{"
																+ StaticVariables.annotationParameterListKeyWord
																+":"
																+"\""
																+ memberValuePair.getName()
																+ "\""
																+ ","
																+ StaticVariables.annotationParameterListValueWord
																+ ":"
//														+ "\""
																+ memberValuePair.getValue().toString()
//														+ "\""
																+ "}";
														if(count != annotationExprParam.getChildrenNodes().size() - 1)
														{
															fileNodeInsertQuery+=",";
														}
													}
												}
												fileNodeInsertQuery += "]'";
												fileNodeInsertQuery += "})";

												// RELATION SHIP Parameter -> ANNOTATION
												fileNodeInsertQuery += ",("
														+ "class"
														+ classNode.getName()
														+ String.valueOf(i)
														+ "method"
														+ methodNode.getName()
														+ String.valueOf(j)
														+ "param"
														+ String.valueOf(k)
														+ ")";

												fileNodeInsertQuery += "-[:"
														+ StaticVariables.has_annotationPropertyName
														+ "]->";
												fileNodeInsertQuery +=
														"(class" + classNode.getName() + String.valueOf(i)
																+ "VARIABLEPARAM"
																+ "parameter" + paramNode.getName()
																+ "ann"
																+ String.valueOf(j)
																+ String.valueOf(k)
																+ String.valueOf(l) + ")";

												// RELATION SHIP FILE -> Annotation
												fileNodeInsertQuery += ",(" + "f" + ")";

												fileNodeInsertQuery += "-[:"
														+ StaticVariables.has_annotationPropertyName
														+ "]->";
												fileNodeInsertQuery += "("
														+ "class"
														+ classNode.getName() + String.valueOf(i)
														+ "VARIABLEPARAM"
														+ "parameter" + paramNode.getName()
														+ "ann"
														+ String.valueOf(j)
														+ String.valueOf(k)
														+ String.valueOf(l) + ")";
											}

										}
									}
								}


								// Throw Method Node
								if (methodNode.getThrowsMethod().size() > 0) {
									for (int k = 0; k < methodNode
											.getThrowsMethod().size(); k++) {
										ThrowMethodNodeAST throwNode = methodNode
												.getThrowsMethod().get(k);
										fileNodeInsertQuery += ",(";
										fileNodeInsertQuery +=
												"class"
														+ classNode.getName()
														+ String.valueOf(i)
														+ "method"
														+ methodNode.getName()
														+ String.valueOf(j) + "throw"
														+ String.valueOf(k)
														+ ":"
														+ StaticVariables.throwNodeName
														+ " {";
										// Throw node property
										fileNodeInsertQuery += StaticVariables.nameThrowPropertyName
												+ ":\'"
												+ throwNode.getName()
												+ "\'";
										fileNodeInsertQuery += "})";

										// RELATION SHIP METHOD -> THROW
										fileNodeInsertQuery += ",("
												+ "class"
												+ classNode.getName()
												+ String.valueOf(i)
												+ "method" + String.valueOf(j)
												+ ")";

										fileNodeInsertQuery += "-[:"
												+ StaticVariables.has_throwPropertyName
												+ "]->";
										fileNodeInsertQuery += "("
												+ "class"
												+ classNode.getName()
												+ String.valueOf(i)
												+ "method"
												+ methodNode.getName()
												+ String.valueOf(j) + "throw"
												+ String.valueOf(k)
												+ ")";
									}
								}
								// RELATION SHIP CLASS -> METHOD
								fileNodeInsertQuery += ",("
										+ "class"
										+ classNode.getName()
										+ String.valueOf(i)
										+ ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.has_methodPropertyName
										+ "]->";
								fileNodeInsertQuery += "("
										+ "class"
										+ classNode.getName()
										+ String.valueOf(i)
										+ "method"
										+ String.valueOf(j)
										+ ")";
							}
						}
						// RELATION SHIP FILE -> CLASS
						fileNodeInsertQuery += ",(" + "f" + ")";

						fileNodeInsertQuery += "-[:"
								+ StaticVariables.has_classPropertyName + "]->";
						fileNodeInsertQuery += "(" + "class"
								+ classNode.getName()
								+ String.valueOf(i) + ")";
						//////////////////////
						// RELATION SHIP CLASS EXTEND
//						if (!classNode.getExtendsClass().equalsIgnoreCase(
//								"None")) {
//
//							//MATCH(<node1-label-name>:<node1-name>),(<node2-label-name>:<node2-name>);
//							//////////////////////////////////
//							fileNodeInsertQuery +="Match(thisClass:"
//							+ classNode.getName()
//							+ "),(extendClass:"
//							+ classNode.getExtendsClass()
//							+ ")";
//							//CREATE(<node1-label-name>)-[<relationship-label-name>:<relationship-name>]->(<node2-label-name>)
//							fileNodeInsertQuery +="CREATE(thisClass)-[extendRelationship:"
//									+ StaticVariables.has_classExtend
//									+ "]->(extendClass)";
//
//							//RETURN <relationship-label-name>
//							fileNodeInsertQuery +="RETURN extendRelationship";
////							fileNodeInsertQuery +="CREATE (";
//							////////////////////////////////
//						}

					}
				}

				// //////////////
				// Interfaces //
				// //////////////

				// List of interfaces
				if (fileNodeAST.getNumberOfInterfaces() > 0) {
					for (int i = 0; i < fileNodeAST.getInterfaces().size(); i++) {
						InterfaceNodeAST interfaceNode = fileNodeAST
								.getInterfaces().get(i);
						fileNodeInsertQuery += ",(";
						fileNodeInsertQuery +=
								"interface"
										+ interfaceNode.getName()
										+ String.valueOf(i) + ":"
										+ StaticVariables.interfaceNodeName + " {";
						// Class node properties
						if (interfaceNode.isHasFinalModifier())
							fileNodeInsertQuery += "hasFinalModifier:\'"
									+ String.valueOf(interfaceNode
									.isHasFinalModifier()) + "\',";
						if (interfaceNode.isHasAbstractModifier())
							fileNodeInsertQuery += "hasAbstractModifier:\'"
									+ String.valueOf(interfaceNode
									.isHasAbstractModifier()) + "\',";
						if (interfaceNode.isHasPrivateModifier())
							fileNodeInsertQuery += "hasPrivateModifier:\'"
									+ String.valueOf(interfaceNode
									.isHasPrivateModifier()) + "\',";
						if (interfaceNode.isHasPublicModifier())
							fileNodeInsertQuery += "hasPublicModifier:\'"
									+ String.valueOf(interfaceNode
									.isHasPublicModifier()) + "\',";
						if (interfaceNode.isHasProtectedModifier())
							fileNodeInsertQuery += "hasProtectedModifier:\'"
									+ String.valueOf(interfaceNode
									.isHasProtectedModifier()) + "\',";
						if (interfaceNode.isHasStaticModifier())
							fileNodeInsertQuery += "hasStaticModifier:\'"
									+ String.valueOf(interfaceNode
									.isHasStaticModifier()) + "\',";
						if (interfaceNode.isHasSynchronizeModifier())
							fileNodeInsertQuery += "hasSynchronizeModifier:\'"
									+ String.valueOf(interfaceNode
									.isHasSynchronizeModifier())
									+ "\',";
						fileNodeInsertQuery += StaticVariables.URLRepoPropertyName
								+ ":\'" + interfaceNode.getRepoURL() + "\',";
						fileNodeInsertQuery += StaticVariables.packageInterfacePropertyName
								+ ":\'"
								+ interfaceNode.getPackageName()
								+ "\',";
						fileNodeInsertQuery += StaticVariables.nameInterfacePropertyName
								+ ":\'" + interfaceNode.getName() + "\'";
						fileNodeInsertQuery += "})";

						// Interface Level Annotation Node
						if (interfaceNode.getAnnotatios().size() > 0) {
							for (int j = 0; j < interfaceNode.getAnnotatios()
									.size(); j++) {
								AnnotationNodeAST annotationNode = interfaceNode
										.getAnnotatios().get(j);
								fileNodeInsertQuery += ",(";
								fileNodeInsertQuery +=
										"interface"
												+ interfaceNode.getName()
												+ String.valueOf(i)
												+ "ann"
												+ String.valueOf(j) + ":"
												+ StaticVariables.annotationNodeName
												+ " {";
								// Annotation node property
								fileNodeInsertQuery += StaticVariables.nameAnnotationPropertyName
										+ ":\'"
										+ annotationNode.getName().replace("\'", "")//.replace("\"", "")
										+ "\',";
								fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
										+ ":\'"
										+ "INTERFACE"
										+ "\'";

								//annotation add parameters as property
								if(annotationNode.getAnnotationParameters().size()!=0)
								{
									fileNodeInsertQuery += ","
											+ StaticVariables.annotationParameterName
											+ ":'[";
									for(int count=0;count<annotationNode.getAnnotationParameters().size();count++)
									{
										fileNodeInsertQuery += "{"
												+ StaticVariables.annotationParameterListKeyWord
												+":"
												+"\""
												+ annotationNode.getAnnotationParameters().get(count).getName()
												+ "\""
												+ ","
												+ StaticVariables.annotationParameterListValueWord
												+ ":"
//														+ "\""
												+ annotationNode.getAnnotationParameters().get(count).getValue().toString()
//														+ "\""
												+ "}";
										if(count!=annotationNode.getAnnotationParameters().size()-1)
										{
											fileNodeInsertQuery+=",";
										}

									}

									fileNodeInsertQuery += "]'";

								}
								fileNodeInsertQuery += "})";

								// RELATION SHIP INTERFACE -> ANNOTATION
								fileNodeInsertQuery += ",("
										+ "interface"
										+ interfaceNode.getName()
										+ String.valueOf(i)
										+ ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.has_annotationPropertyName
										+ "]->";
								fileNodeInsertQuery += "("
										+ "interface"
										+ interfaceNode.getName()
										+ String.valueOf(i)
										+ "ann"
										+ String.valueOf(j) + ")";

								// RELATION SHIP FILE -> Annotation
								fileNodeInsertQuery += ",(" + "f" + ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.has_annotationPropertyName
										+ "]->";
								fileNodeInsertQuery += "("
										+ "interface"
										+ interfaceNode.getName()
										+ String.valueOf(i)
										+ "ann"
										+ String.valueOf(j) + ")";
							}
						}



						// all parameter level annotation node ; from interface to parameter
						List<FieldDeclaration> parametersInClass = interfaceNode.getChildClasses();
						for (int j = 0; j < parametersInClass.size(); j ++) {
							// all parameters
							FieldDeclaration parameter = parametersInClass.get(j);
							ParameterMethodNodeAST parameterMethodNodeAST = new ParameterMethodNodeAST(
									parameter.getType().toString(),
									parameter.getVariables().get(0).getId().toString()
							);
							fileNodeInsertQuery += ",(";
							fileNodeInsertQuery +=
									"interface" + interfaceNode.getName()
											+ String.valueOf(i)
											+ "FIELD"
											+ "param"
											+ parameterMethodNodeAST.getName()
											+ String.valueOf(j)
											+ ":"
											+ StaticVariables.parameterNodeName
											+ " {";
							// Parameter node property
							fileNodeInsertQuery += StaticVariables.nameParameterPropertyName
									+ ":\'"
									+ parameterMethodNodeAST.getName()
									+ "\',";
							fileNodeInsertQuery += StaticVariables.typeParameterPropertyName
									+ ":\'"
									+ parameterMethodNodeAST.getType()
									+ "\',";
							fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
									+ ":\'"
									+ "FIELD"
									+ "\'";
							fileNodeInsertQuery += "})";

							// RELATION SHIP Class -> Parameter
							fileNodeInsertQuery += ",("
									+ "interface"
									+ interfaceNode.getName()
									+ String.valueOf(i)
									+ ")";

							fileNodeInsertQuery += "-[:"
									+ StaticVariables.has_parameterPropertyName
									+ "]->";
							fileNodeInsertQuery += "(" +
									"interface" + interfaceNode.getName()
									+ String.valueOf(i)
									+ "FIELD"
									+ "param"
									+ parameterMethodNodeAST.getName()
									+ String.valueOf(j) + ")";

							// parameter level annotation
							List<AnnotationExpr> annotationExprs =  parameter.getAnnotations();
							for (int k = 0; k < annotationExprs.size(); k ++) {
								fileNodeInsertQuery += ",(";
								fileNodeInsertQuery +=
										"interface" + interfaceNode.getName() + String.valueOf(i) +
												"parameter" + parameterMethodNodeAST.getName()
												+ "ann"
												+ String.valueOf(j)
												+ String.valueOf(k) + ":"
												+ StaticVariables.annotationNodeName
												+ " {";

								// Annotation node property
								AnnotationExpr annotationExpr = annotationExprs.get(k);
								fileNodeInsertQuery += StaticVariables.nameAnnotationPropertyName
										+ ":\'"
										+ "@" + annotationExpr.getName().toString().replace("\'", "")//.replace("\"", "|")    //将双引号变成|
										+ "\',";
								fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
										+ ":\'"
										+ "FIELD"
										+ "\'";
								//annotation add parameters as property
								// annotation's parameters
								fileNodeInsertQuery += ","
										+ StaticVariables.annotationParameterName
										+ ":'[";
								// first one is annotation itself
								if (annotationExpr.getChildrenNodes().size() > 1) {
									for(int count=1; count < annotationExpr.getChildrenNodes().size(); count++)
									{
										MemberValuePair memberValuePair = (MemberValuePair) annotationExpr.getChildrenNodes().get(count);
										fileNodeInsertQuery += "{"
												+ StaticVariables.annotationParameterListKeyWord
												+":"
												+"\""
												+ memberValuePair.getName()
												+ "\""
												+ ","
												+ StaticVariables.annotationParameterListValueWord
												+ ":"
//														+ "\""
												+ memberValuePair.getValue().toString()
//														+ "\""
												+ "}";
										if(count != annotationExpr.getChildrenNodes().size() - 1)
										{
											fileNodeInsertQuery+=",";
										}
									}
								}
								fileNodeInsertQuery += "]'";

								fileNodeInsertQuery += "})";
								// RELATION SHIP Parameter -> ANNOTATION
								fileNodeInsertQuery += ",("
										+ "interface"
										+ interfaceNode.getName()
										+ String.valueOf(i)
										+ "FIELD"
										+ "param"
										+ parameterMethodNodeAST.getName()
										+ String.valueOf(j)
										+ ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.has_annotationPropertyName
										+ "]->";
								fileNodeInsertQuery += "(" +
										"interface" + interfaceNode.getName() + String.valueOf(i) +
										"parameter" + parameterMethodNodeAST.getName()
										+ "ann"
										+ String.valueOf(j)
										+ String.valueOf(k) + ")";

								// RELATION SHIP FILE -> Annotation
								fileNodeInsertQuery += ",(" + "f" + ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.has_annotationPropertyName
										+ "]->";
								fileNodeInsertQuery += "("
										+ "interface"
										+ interfaceNode.getName()
										+ String.valueOf(i)
										+ "ann"
										+ String.valueOf(j) + ")";
							}

						}

						// Method Node
						if (interfaceNode.getMethod().size() > 0) {
							for (int j = 0; j < interfaceNode.getMethod()
									.size(); j++) {
								InterfaceHasMethodNodeAST methodNode = interfaceNode
										.getMethod().get(j);
								fileNodeInsertQuery += ",(";
								fileNodeInsertQuery +=
										"interface"
												+ interfaceNode.getName()
												+ String.valueOf(i) + "method"
												+ String.valueOf(j) + ":"
												+ StaticVariables.methodNodeName + " {";

								if (methodNode.isHasFinalModifier())
									fileNodeInsertQuery += "hasFinalModifier:\'"
											+ String.valueOf(methodNode
											.isHasFinalModifier())
											+ "\',";
								if (methodNode.isHasAbstractModifier())
									fileNodeInsertQuery += "hasAbstractModifier:\'"
											+ String.valueOf(methodNode
											.isHasAbstractModifier())
											+ "\',";
								if (methodNode.isHasPrivateModifier())
									fileNodeInsertQuery += "hasPrivateModifier:\'"
											+ String.valueOf(methodNode
											.isHasPrivateModifier())
											+ "\',";
								if (methodNode.isHasPublicModifier())
									fileNodeInsertQuery += "hasPublicModifier:\'"
											+ String.valueOf(methodNode
											.isHasPublicModifier())
											+ "\',";
								if (methodNode.isHasProtectedModifier())
									fileNodeInsertQuery += "hasProtectedModifier:\'"
											+ String.valueOf(methodNode
											.isHasProtectedModifier())
											+ "\',";
								if (methodNode.isHasStaticModifier())
									fileNodeInsertQuery += "hasStaticModifier:\'"
											+ String.valueOf(methodNode
											.isHasStaticModifier())
											+ "\',";
								if (methodNode.isHasSynchronizeModifier())
									fileNodeInsertQuery += "hasSynchronizeModifier:\'"
											+ String.valueOf(methodNode
											.isHasSynchronizeModifier())
											+ "\',";

								fileNodeInsertQuery += StaticVariables.returningTypeMethodPropertyName
										+ ":\'"
										+ methodNode.getReturningType()
										+ "\',";
								fileNodeInsertQuery += StaticVariables.URLRepoPropertyName
										+ ":\'" + interfaceNode.getRepoURL() + "\',";
								fileNodeInsertQuery += StaticVariables.packageMethodPropertyName
										+ ":\'"
										+ methodNode.getPackageName()
										+ "\',";
								fileNodeInsertQuery += StaticVariables.nameMethodPropertyName
										+ ":\'" + methodNode.getName() + "\'";
								fileNodeInsertQuery += "})";

								// Method's RelationShips

								// Method Level Annotation Node
								if (methodNode.getAnnotatios().size() > 0) {
									for (int k = 0; k < methodNode
											.getAnnotatios().size(); k++) {
										AnnotationNodeAST annotationNode = methodNode
												.getAnnotatios().get(k);
										fileNodeInsertQuery += ",(";
										fileNodeInsertQuery +=
												"interface"
														+ interfaceNode.getName()
														+ String.valueOf(i)
														+ "method"
														+ methodNode.getName()
														+ String.valueOf(j)
														+ "ann"
														+ String.valueOf(k)
														+ ":"
														+ StaticVariables.annotationNodeName
														+ " {";
										// Annotation node property
										fileNodeInsertQuery += StaticVariables.nameAnnotationPropertyName
												+ ":\'"
												+ annotationNode.getName().replace("\'", "")//.replace("\"", "")
												+ "\',";
										fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
												+ ":\'"
												+ "METHOD"
												+ "\'";
										//annotation add parameters as property
										//   eg.   sequence:'[{key:"tag",value:"代码生成器"},{key:"config",value:"test"}]'

										if(annotationNode.getAnnotationParameters().size()!=0)
										{
											fileNodeInsertQuery += ","
													+ StaticVariables.annotationParameterName
													+ ":'[";
											for(int count=0;count<annotationNode.getAnnotationParameters().size();count++)
											{
												fileNodeInsertQuery += "{"
														+ StaticVariables.annotationParameterListKeyWord
														+":"
														+"\""
														+ annotationNode.getAnnotationParameters().get(count).getName()
														+ "\""
														+ ","
														+ StaticVariables.annotationParameterListValueWord
														+ ":"
//														+ "\""
														+ annotationNode.getAnnotationParameters().get(count).getValue().toString()
//														+ "\""
														+ "}";
												if(count!=annotationNode.getAnnotationParameters().size()-1)
												{
													fileNodeInsertQuery+=",";
												}

											}

											fileNodeInsertQuery += "]'";

										}
										fileNodeInsertQuery += "})";

										// RELATION SHIP METHOD -> ANNOTATION
										fileNodeInsertQuery += ",("
												+ "interface"
												+ interfaceNode.getName()
												+ String.valueOf(i)
												+ "method" + String.valueOf(j)
												+ ")";

										fileNodeInsertQuery += "-[:"
												+ StaticVariables.has_annotationPropertyName
												+ "]->";
										fileNodeInsertQuery += "("
												+ "interface"
												+ interfaceNode.getName()
												+ String.valueOf(i)
												+ "method"
												+ methodNode.getName()
												+ String.valueOf(j) + "ann"
												+ String.valueOf(k) + ")";

										// RELATION SHIP FILE -> Annotation
										fileNodeInsertQuery += ",(" + "f" + ")";

										fileNodeInsertQuery += "-[:"
												+ StaticVariables.has_annotationPropertyName
												+ "]->";
										fileNodeInsertQuery += "("
												+ "interface"
												+ interfaceNode.getName()
												+ String.valueOf(i)
												+ "method"
												+ methodNode.getName()
												+ String.valueOf(j) + "ann"
												+ String.valueOf(k) + ")";
									}
								}

								// Parameter Node
								if (methodNode.getParametersMethod().size() > 0) {
									for (int k = 0; k < methodNode.getParametersMethod().size(); k++) {
										ParameterNodeAST paramNode = methodNode.getParametersMethod().get(k);
										fileNodeInsertQuery += ",(";
										fileNodeInsertQuery +=
												"interface"
														+ interfaceNode.getName()
														+ String.valueOf(i)
														+ "method"
														+ methodNode.getName()
														+ String.valueOf(j)
														+ "param"
														+ String.valueOf(k)
														+ ":"
														+ StaticVariables.parameterNodeName
														+ " {";
										// Parameter node property
										fileNodeInsertQuery += StaticVariables.nameParameterPropertyName
												+ ":\'"
												+ paramNode.getName()
												+ "\',";
										fileNodeInsertQuery += StaticVariables.typeParameterPropertyName
												+ ":\'"
												+ paramNode.getType()
												+ "\',";
										fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
												+ ":\'"
												+ paramNode.getTarget()
												+ "\'";
										fileNodeInsertQuery += "})";

										// RELATION SHIP METHOD -> PARAMETER
										fileNodeInsertQuery += ",("
												+ "interface"
												+ interfaceNode.getName()
												+ String.valueOf(i)
												+ "method" + String.valueOf(j)
												+ ")";

										fileNodeInsertQuery += "-[:"
												+ StaticVariables.has_parameterPropertyName
												+ "]->";
										fileNodeInsertQuery += "("
												+ "interface"
												+ interfaceNode.getName()
												+ String.valueOf(i)
												+ "method"
												+ methodNode.getName()
												+ String.valueOf(j) + "param"
												+ String.valueOf(k)
												+ ")";

										// parameter level annotation
										List<AnnotationExpr> paramAnnotations = paramNode.getAnnotations();
										if (paramAnnotations.size() > 0) {
											for (int l = 0; l < paramAnnotations.size(); l ++) {
												fileNodeInsertQuery += ",(";
												fileNodeInsertQuery +=
														"interface" + interfaceNode.getName() + String.valueOf(i)
																+ "VARIABLEPARAM"
																+ "parameter" + paramNode.getName()
																+ "ann"
																+ String.valueOf(j)
																+ String.valueOf(k)
																+ String.valueOf(l) + ":"
																+ StaticVariables.annotationNodeName
																+ " {";
												// Annotation node property
												AnnotationExpr annotationExprParam = paramAnnotations.get(l);
												fileNodeInsertQuery += StaticVariables.nameAnnotationPropertyName
														+ ":\'"
														+ "@" + annotationExprParam.getName().toString().replace("\'", "")//.replace("\"", "|")    //将双引号变成|
														+ "\',";
												fileNodeInsertQuery += StaticVariables.targetParameterPropertyName
														+ ":\'"
														+ paramNode.getTarget()
														+ "\'";
												//annotation add parameters as property
												// annotation's parameters
												fileNodeInsertQuery += ","
														+ StaticVariables.annotationParameterName
														+ ":'[";
												// first one is annotation itself
												if (annotationExprParam.getChildrenNodes().size() > 1) {
													for(int count=1; count < annotationExprParam.getChildrenNodes().size(); count++)
													{
														MemberValuePair memberValuePair = (MemberValuePair) annotationExprParam.getChildrenNodes().get(count);
														fileNodeInsertQuery += "{"
																+ StaticVariables.annotationParameterListKeyWord
																+":"
																+"\""
																+ memberValuePair.getName()
																+ "\""
																+ ","
																+ StaticVariables.annotationParameterListValueWord
																+ ":"
//														+ "\""
																+ memberValuePair.getValue().toString()
//														+ "\""
																+ "}";
														if(count != annotationExprParam.getChildrenNodes().size() - 1)
														{
															fileNodeInsertQuery+=",";
														}
													}
												}
												fileNodeInsertQuery += "]'";
												fileNodeInsertQuery += "})";

												// RELATION SHIP Parameter -> ANNOTATION
												fileNodeInsertQuery += ",("
														+ "interface"
														+ interfaceNode.getName()
														+ String.valueOf(i)
														+ "method"
														+ methodNode.getName()
														+ String.valueOf(j)
														+ "param"
														+ String.valueOf(k)
														+ ")";

												fileNodeInsertQuery += "-[:"
														+ StaticVariables.has_annotationPropertyName
														+ "]->";
												fileNodeInsertQuery += "(" +
														"interface" + interfaceNode.getName() + String.valueOf(i)
														+ "VARIABLEPARAM"
														+ "parameter" + paramNode.getName()
														+ "ann"
														+ String.valueOf(j)
														+ String.valueOf(k)
														+ String.valueOf(l)
														+ ")";

												// RELATION SHIP FILE -> Annotation
												fileNodeInsertQuery += ",(" + "f" + ")";

												fileNodeInsertQuery += "-[:"
														+ StaticVariables.has_annotationPropertyName
														+ "]->";
												fileNodeInsertQuery += "("
														+ "interface" + interfaceNode.getName() + String.valueOf(i)
														+ "VARIABLEPARAM"
														+ "parameter" + paramNode.getName()
														+ "ann"
														+ String.valueOf(j)
														+ String.valueOf(k)
														+ String.valueOf(l) + ")";
											}

										}
									}
								}

								// Throw Method Node
								if (methodNode.getThrowsMethod().size() > 0) {
									for (int k = 0; k < methodNode
											.getThrowsMethod().size(); k++) {
										ThrowMethodNodeAST throwNode = methodNode
												.getThrowsMethod().get(k);
										fileNodeInsertQuery += ",(";
										fileNodeInsertQuery +=
												"interface"
														+ interfaceNode.getName()
														+ String.valueOf(i)
														+ "method"
														+ methodNode.getName()
														+ String.valueOf(j) + "throw"
														+ String.valueOf(k) + ":"
														+ StaticVariables.throwNodeName
														+ " {";
										// Throw node property
										fileNodeInsertQuery += StaticVariables.nameThrowPropertyName
												+ ":\'"
												+ throwNode.getName()
												+ "\'";
										fileNodeInsertQuery += "})";

										// RELATION SHIP METHOD -> THROW
										fileNodeInsertQuery += ",("
												+ "interface"
												+ interfaceNode.getName()
												+ String.valueOf(i)
												+ "method" + String.valueOf(j)
												+ ")";

										fileNodeInsertQuery += "-[:"
												+ StaticVariables.has_throwPropertyName
												+ "]->";
										fileNodeInsertQuery += "("
												+ "interface"
												+ interfaceNode.getName()
												+ String.valueOf(i)
												+ "method"
												+ methodNode.getName()
												+ String.valueOf(j) + "throw"
												+ String.valueOf(k) + ")";
									}
								}
								// RELATION SHIP INTERFACE -> METHOD
								fileNodeInsertQuery += ",(" + "interface"
										+ interfaceNode.getName()
										+ String.valueOf(i) + ")";

								fileNodeInsertQuery += "-[:"
										+ StaticVariables.has_methodPropertyName
										+ "]->";
								fileNodeInsertQuery += "(" + "interface"
										+ interfaceNode.getName()
										+ String.valueOf(i) + "method"
										+ String.valueOf(j) + ")";
							}
						}
						// RELATION SHIP FILE -> CLASS
						fileNodeInsertQuery += ",(" + "f" + ")";

						fileNodeInsertQuery += "-[:"
								+ StaticVariables.has_interfacePropertyName
								+ "]->";
						fileNodeInsertQuery += "(" + "interface"
								+ interfaceNode.getName()
								+ String.valueOf(i) + ")";
					}
				}

				fileNodeInsertQuery += ";";


				// Insert query on Neo4j graph DB
				session.run(fileNodeInsertQuery);

				logger.info("Insertion Query: " + fileNodeInsertQuery);
				resultLog.info(fileNodeInsertQuery);

			} catch (Exception e) {
				logger.debug("Excetion : ", e);
				debugLog.debug("Excetion : ", e);
				return;
			}
		} else {
			logger.debug("Driver or Session is down, check the configuration");
			debugLog.debug("Driver or Session is down, check the configuration");
		}
	}

	public void insertRepoNodeNeo4JDB(String repoURL, long linesOfJavaCode) {

		if (isNeo4jConnectionUp()) {
			// File Node of AST
			String nodeInsertQuery = "CREATE (";
			nodeInsertQuery += "r:" + StaticVariables.repoNodeName + " {";
			// File node properties
			nodeInsertQuery += StaticVariables.URLRepoPropertyName + ":\'"
					+ repoURL + "\',";
			nodeInsertQuery += StaticVariables.linesOfJavaCodeRepoPropertyName
					+ ":" + String.valueOf(linesOfJavaCode) + "";
			nodeInsertQuery += "})";

			nodeInsertQuery += ";";
			logger.info("Insertion Query: " + nodeInsertQuery);
			resultLog.info(nodeInsertQuery);

			// Insert query on Neo4j graph DB
			session.run(nodeInsertQuery);

		} else {
			logger.debug("Driver or Session is down, check the configuration");
			debugLog.debug("Driver or Session is down, check the configuration");
		}
	}

	/*
	 * Close Neo4j Connection
	 */
	public void closeDriverSession() {
		if (session != null)
			session.close();
		if (driver != null)
			driver.close();
	}

	public String escapingCharacters(String query) {

		return query;
	}

	/*
	 * Check Neo4j Connection
	 */
	public boolean isNeo4jConnectionUp() {
		return session.isOpen();
	}

}
