package com.elasticthree.ASTCreator.ASTCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;//

import com.elasticthree.ASTCreator.ASTCreator.Neo4jDriver.Neo4JInsertClassRelation;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.elasticthree.ASTCreator.ASTCreator.Helpers.PathParser;
import com.elasticthree.ASTCreator.ASTCreator.Objects.Project;

import com.elasticthree.ASTCreator.ASTCreator.Helpers.RecursivelyProjectJavaFiles;
import com.elasticthree.ASTCreator.ASTCreator.Neo4jDriver.Neo4JDriver;
import com.elasticthree.ASTCreator.ASTCreator.Objects.FileNodeAST;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import org.xml.sax.SAXException;

public class ASTCreator {

	static final Logger stdoutLog = Logger.getLogger(ASTCreator.class);
	static final Logger debugLog = Logger.getLogger("debugLogger");

	private String repoURL;
	private long javaLinesOfCode;
	private Neo4JDriver neo4j;
	private Neo4JInsertClassRelation neo4j2;
	//private List<Project> pomList;

	public ASTCreator(String repoURL) {
		setRepoURL(repoURL);
		setJavaLinesOfCode(0);
		neo4j = new Neo4JDriver();
		neo4j2 = new Neo4JInsertClassRelation();
	}

	/**
	 * We use CompilationUnit (from Javaparser project) to parse the File
	 * 
	 * @param path_to_class
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public CompilationUnit getClassCompilationUnit(String path_to_class) {
		// creates an input stream for the file to be parsed
		FileInputStream in = null;
		CompilationUnit cu = null;
		try {
			in = new FileInputStream(path_to_class);
		} catch (FileNotFoundException e) {
			debugLog.debug("IO Error skip project " + path_to_class
					+ " from AST - Graph procedure");
			return cu;
		}
		try {
			cu = JavaParser.parse(in);
		} catch (Exception e1) {
			debugLog.debug("Parsing Error skip project " + path_to_class
					+ " from AST - Graph procedure");
		}
		try {
			in.close();
		} catch (IOException e) {
			debugLog.debug("IO Error skip project " + path_to_class
					+ " from AST - Graph procedure");
		}
		return cu;
	}


	//this function can get file property: artifactId from package property
//	public String getFileArtifactId(String packageName)
//	{
//		String artifactId="";
//
//
//		//get document from path==>the path of .xml
//		try {
//			String path="";/////
//			File f = new File(path);
//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder builder = null;
//			builder = factory.newDocumentBuilder();
//			Document doc = builder.parse(f);
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//		} catch (SAXException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//
//		return artifactId;
//	}

	/**
	this function creates fileNodeAST property: artifactId
	 from file absPath to pomList.artifactId
	 */

	public int compareString(String a,String b) //used by function getFileArtifactId
	{
		for(int j=0;j<a.length()&&j<b.length();j++)
		{
			if(a.charAt(j)!=b.charAt(j))
			{
				return j;
			}
		}
		return 0;

	}
	public String getFileArtifactId(String fileAbsPath,List<Project>pomList)
	{
		int count=0;
		int max=0;
		for(int i=0;i<pomList.size();i++)
		{
			String pomPathCur=pomList.get(i).getPomPath();
			//compare pomPathCur with fileAbsPath
			int cur=compareString(fileAbsPath,pomPathCur);
			if(cur>max)
			{
				max=cur;
				count=i;
			}
		}
		return pomList.get(count).getArtifactId();

	}


	/**
	 * This function creates AST (Abstract syntax tree) for a Java file
	 * 
	 * @param path_to_class
	 * @return
	 */

	public FileNodeAST getASTFileObject(String path_to_class,List<Project>pomList) {

		FileNodeAST fileObject = null;
		CompilationUnit cu;
		cu = getClassCompilationUnit(path_to_class);
		if (cu != null) {
			ClassMethodDeclarationAST ast = new ClassMethodDeclarationAST(cu,
					getRepoURL(), path_to_class);
			ast.getTypeDeclarationFile();
			String packageName = "";
			try {
				packageName = cu.getPackage().getName().toString();
			} catch (NullPointerException n_e) {
				packageName = "No_package";
			}
			fileObject = new FileNodeAST(getRepoURL(), path_to_class,
					packageName, ast.getClassVisitor().getNumberOfClasses(),
					ast.getClassVisitor().getNumberOfInterfaces());
			fileObject.setArtifactId(getFileArtifactId(fileObject.getAbsPathToFile(),pomList));

			fileObject.setClasses(ast.getClassVisitor().getClasses());
			fileObject.setInterfaces(ast.getClassVisitor().getInterfaces());
		}
		return fileObject;
	}

	/**
	 * This function runs the AST and inserts it in Neo4j instance for all Java
	 * files of a Java Project
	 * 
	 * @param classes
	 */
	public void repoASTProcedure(List<String> classes,List<Project>pomList) {
		classes.forEach(file -> {
			stdoutLog.info("-> Java File: " + file);
			FileNodeAST fileNode = getASTFileObject(file,pomList);
			if (fileNode != null) {
				neo4j.insertNeo4JDB(fileNode);
				Stream<String> lines = null;
				try {
					lines = Files.lines(Paths.get(file));
					addJavaLinesOfCode(lines.count());
				} catch (Exception e) {
					stdoutLog.debug("Error", e);
					debugLog.debug("Error", e);
				}
				finally {
					if (lines != null)
						lines.close();
				}
			}
		});
		//遍历第二遍所有的file，在每个file中遍历class，添加class之间的关系
		classes.forEach(file -> {
			stdoutLog.info("-> Java File: " + file);
			FileNodeAST fileNode = getASTFileObject(file,pomList);
			if (fileNode != null) {
				neo4j2.insertClassRelationshipNeo4JDB(fileNode);
				Stream<String> lines = null;
				try {
					lines = Files.lines(Paths.get(file));
					addJavaLinesOfCode(lines.count());
				} catch (Exception e) {
					stdoutLog.debug("Error", e);
					debugLog.debug("Error", e);
				}
				finally {
					if (lines != null)
						lines.close();
				}
			}
		});


		neo4j.insertRepoNodeNeo4JDB(getRepoURL(), getJavaLinesOfCode());
		//
		neo4j.closeDriverSession();
//		neo4j2.closeDriverSession();
	}


	public static void main(String[] args) {
		PropertyConfigurator.configure("resources/log4j.properties");
		// args[0] -> Path to Java Project
		List<String> classes = RecursivelyProjectJavaFiles
				.getProjectJavaFiles(args[0]);
		// args[1] -> URL of Java Project
		ASTCreator ast = new ASTCreator(args[1]);


		//初始化 xmlPath
		File file = new File(args[0]);
		PathParser pathParse = new PathParser();
		List<String> xmlPath = pathParse.generateXmlPath(file);
		List<Project> pomList = new ArrayList<>();

		//print
//		for (String s : xmlPath) {
//			System.out.println("路径" + s);
//		}
//		System.out.println(xmlPath.size());


		xmlPath.sort(new Comparator<String>() {
			@Override
			public int compare(String s, String t1) {
				return s.length() - t1.length();
			}
		});
		PathParser parse = new PathParser();
		for (String s : xmlPath) {

			pomList.add(parse.parseXml(s));


		}
		ast.repoASTProcedure(classes,pomList);


	}

	public Neo4JDriver getNeo4j() {
		return neo4j;
	}

	public void setNeo4j(Neo4JDriver neo4j) {
		this.neo4j = neo4j;
	}

	public String getRepoURL() {
		return repoURL;
	}

	public void setRepoURL(String repoURL) {
		this.repoURL = repoURL;
	}

	public long getJavaLinesOfCode() {
		return javaLinesOfCode;
	}

	public void setJavaLinesOfCode(long javaLinesOfCode) {
		this.javaLinesOfCode = javaLinesOfCode;
	}

	public void addJavaLinesOfCode(long javaLinesOfCode) {
		this.javaLinesOfCode += javaLinesOfCode;
	}

}
