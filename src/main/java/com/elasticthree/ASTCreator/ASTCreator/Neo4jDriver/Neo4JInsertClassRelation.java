package com.elasticthree.ASTCreator.ASTCreator.Neo4jDriver;

import com.elasticthree.ASTCreator.ASTCreator.Helpers.StaticVariables;
import com.elasticthree.ASTCreator.ASTCreator.Objects.*;

import org.apache.log4j.Logger;
import org.neo4j.driver.*;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Neo4JInsertClassRelation {
    final static Logger logger = Logger.getLogger(Neo4JInsertClassRelation.class);
    final static Logger debugLog = Logger.getLogger("debugLogger");
    final static Logger resultLog = Logger.getLogger("reportsLogger");

    private String host;
    private String usern;
    private String password;
    private Driver driver;
    private Session session;

    private List<String> dependencyClasses;

    /**
     * Neo4JDriver creates and inserts the query to Neo4j instance
     */
    public Neo4JInsertClassRelation() {
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
//    public void insertNeo4JDBLogFile(String query) {
//
//        if (isNeo4jConnectionUp()) {
//
//            try {
//                // Insert query on Neo4j graph DB
//                session.run(query);
//                logger.info("Insertion Query: " + query);
//
//            } catch (Exception e) {
//                logger.debug("Excetion : ", e);
//                debugLog.debug("Excetion : ", e);
//                return;
//            }
//        } else {
//            logger.debug("Driver or Session is down, check the configuration");
//            debugLog.debug("Driver or Session is down, check the configuration");
//        }
//
//    }

    /**
     * Inserting AST to Neo4J instance
     *
     * @param fileNodeAST
     *            (AST root node)
     */
    public void insertClassRelationshipNeo4JDB(FileNodeAST fileNodeAST) {

        if (fileNodeAST == null) {
            logger.debug("AST File Object is null (Probably had parsing error)");
            debugLog.debug("AST File Object is null (Probably had parsing error)");
            return;
        }

        if (isNeo4jConnectionUp()) {
            try {
                // File Node of AST
                String fileNodeInsertQuery = "";

                // List of Classes
                if (fileNodeAST.getNumberOfClasses() > 0) {

                    for (int i = 0; i < fileNodeAST.getClasses().size(); i++) {
                        ClassNodeAST classNode = fileNodeAST.getClasses()
                                .get(i);
                        //////////////////////
                        // RELATION SHIP CLASS EXTEND
                        if (!classNode.getExtendsClass().equalsIgnoreCase(
                                "None")) {

//                                CQL语法:
//                            Match(thisClass:Class),(extendClass:Class)
//                            WHERE thisClass.name="FileInfo" AND extendClass.name="FileUtil"
//                            CREATE(thisClass)-[extendRelationship:EXTENDS]->(extendClass)
//                            RETURN extendRelationship;

                            fileNodeInsertQuery +="Match(thisClass:Class),(extendClass:Class)\n" +
                                    "WHERE thisClass.name=\""
                                    + classNode.getName()
                                    + "\" AND extendClass.name=\""
                                    + classNode.getExtendsClass()
                                    + "\" \n" +
                                    "CREATE UNIQUE (thisClass)-[extendRelationship:EXTENDS]->(extendClass)\n" +
                                    "RETURN extendRelationship;";

                            if(!fileNodeInsertQuery.equals("")){
                                // Insert query on Neo4j graph DB
                                session.run(fileNodeInsertQuery);

                                logger.info("Insertion Query: " + fileNodeInsertQuery);
                                resultLog.info(fileNodeInsertQuery);

                                fileNodeInsertQuery = "";
                            }

                        }


                        List<String> d = classNode.getDependencyClasses();
                        if(classNode.getDependencyClasses().size()!=0)
                        {
                            for(int j=0;j<classNode.getDependencyClasses().size();j++)
                            {
                                if(classNode.getName().toString()!=classNode.getDependencyClasses().get(j).toString())
                                {
                                    fileNodeInsertQuery +="Match(thisClass:Class),(dependencyClass:Class)\n" +
                                            "WHERE thisClass.name=\""
                                            + classNode.getName()
                                            + "\" AND dependencyClass.name=\""
                                            + classNode.getDependencyClasses().get(j).toString()
                                            + "\" \n" +
                                            "CREATE(thisClass)-[dependencyRelationship:DEPENDENCY]->(dependencyClass)\n" +
                                            "RETURN dependencyRelationship;";
                                }


                                if(!fileNodeInsertQuery.equals("")){
                                    // Insert query on Neo4j graph DB
                                    session.run(fileNodeInsertQuery);

                                    logger.info("Insertion Query: " + fileNodeInsertQuery);
                                    resultLog.info(fileNodeInsertQuery);

                                    fileNodeInsertQuery = "";
                                }
                            }

                        }



                        // Add method parameter type node
                        // Method Node
                        if(classNode.getMethod().size()>0){
                            for(int j=0;j<classNode.getMethod().size();j++){
                                ClassHasMethodNodeAST methodNode = classNode.getMethod().get(j);

                                // Parameter Node
                                if(methodNode.getParameters().size()>0){
                                    for(int k = 0;k<methodNode.getParameters().size();k++){
                                        ParameterMethodNodeAST paramNode = methodNode.getParameters().get(k);


                                        // Find if the parameter type is a existing class node
//                                        String paramTypeNodeQuery = "MATCH (class:Class) WHERE class.name = '"+paramNode.getType()+"' RETURN class;";//paramNode.getType()
//                                        boolean hasNext = false;//= result.hasNext();
//
//                                        StatementResult result = session.run(paramTypeNodeQuery);
////                                        sessionTemp.close();
//
//                                        try{
//                                            hasNext = result.hasNext();
//                                            result.consume();
//                                        }catch (Exception ex){
//                                            System.out.println("expect one statement per query but find 2 - "+paramNode.getType());
//                                        }
                                        boolean isClassExist = false;
                                        if(Neo4JDriver.ClassList.contains(paramNode.getType())){
                                            isClassExist = true;
                                        }
//                                        if (fileNodeAST.getNumberOfClasses() > 0) {
//                                            for (int i1 = 0; i1 < fileNodeAST.getClasses().size(); i1++) {
//                                                ClassNodeAST classNodeTemp = fileNodeAST.getClasses()
//                                                        .get(i1);
//
//                                                if(classNodeTemp.getName() == paramNode.getType()){
//                                                    isClassExist = true;
//                                                    break;
//                                                }
//                                            }
//                                        }



                                        if(!isClassExist){
                                            // Param node doesn't exist
                                            // Insert ParameterTypeNdoe query
                                            fileNodeInsertQuery += "MERGE (type:parameter_type{name:'"+paramNode.getType()+"'});";

                                            if(!fileNodeInsertQuery.equals("")){
                                                // Insert query on Neo4j graph DB
                                                session.run(fileNodeInsertQuery);

                                                logger.info("Insertion Query: " + fileNodeInsertQuery);
                                                resultLog.info(fileNodeInsertQuery);

                                                fileNodeInsertQuery = "";
                                            }

                                            // Insert Relationship query
                                            fileNodeInsertQuery += "Match(parameter:Parameter),(parameterType:parameter_type)\n" +
                                                    "WHERE parameter.name=\""
                                                    + paramNode.getName()
                                                    + "\" AND parameter.type=\""
                                                    + paramNode.getType()
                                                    + "\" AND parameterType.name=\""
                                                    + paramNode.getType()
                                                    + "\" \n" +
                                                    "CREATE UNIQUE(parameter)-[parameterTypeRelationship:HAS_PARAMETER_TYPE]->(parameterType)\n" +
                                                    "RETURN parameterTypeRelationship;";
                                        }else{
                                            // Param node already exists
                                            // Don't need to insert parameter type node

                                            // Insert Relationship query
                                            fileNodeInsertQuery += "Match(parameter:Parameter),(parameterType:Class)\n" +
                                                    "WHERE parameter.name=\""
                                                    + paramNode.getName()
                                                    + "\" AND parameter.type=\""
                                                    + paramNode.getType()
                                                    + "\" AND parameterType.name=\""
                                                    + paramNode.getType()
                                                    + "\" \n" +
                                                    "CREATE UNIQUE(parameter)-[parameterTypeRelationship:HAS_PARAMETER_TYPE]->(parameterType)\n" +
                                                    "RETURN parameterTypeRelationship;";
                                        }

                                        if(!fileNodeInsertQuery.equals("")){
                                            // Insert query on Neo4j graph DB
                                            session.run(fileNodeInsertQuery);

                                            logger.info("Insertion Query: " + fileNodeInsertQuery);
                                            resultLog.info(fileNodeInsertQuery);

                                            fileNodeInsertQuery = "";
                                        }
                                    }

                                }


                            }
                        }



                    }
                }

                //match file and module
                fileNodeInsertQuery+="Match(file:File),(module:Module)\n" +
                        "WHERE file.moduleArtifactId=module.artifactId\n" +
                        "CREATE UNIQUE (file)-[pertainRelationship:PERTAINS]->(module)\n" +
                        "RETURN pertainRelationship;";
                session.run(fileNodeInsertQuery);

                logger.info("Insertion Query: " + fileNodeInsertQuery);
                resultLog.info(fileNodeInsertQuery);

                //fileNodeInsertQuery = "";


//                fileNodeInsertQuery += ";";




//                if(!fileNodeInsertQuery.equals("")){
//                    // Insert query on Neo4j graph DB
//                    session.run(fileNodeInsertQuery);
//
//                    logger.info("Insertion Query: " + fileNodeInsertQuery);
//                    resultLog.info(fileNodeInsertQuery);
//                }


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
