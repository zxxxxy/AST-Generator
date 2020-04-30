package com.elasticthree.ASTCreator.ASTCreator.Helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.w3c.dom.NodeList;

import com.elasticthree.ASTCreator.ASTCreator.Objects.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class PathParser {

//    String filePath;
    private List<String> xmlPathList = new ArrayList<>();

    private static Map<String, String> propertiesMap = new HashMap<>();

//    public PathParser (){
//
//        //this.filePath=filePath;
//
//    }
    public List<String> generateXmlPath(File file){
        File[] fs = file.listFiles();
        String srcPattern = ".*src.*";
        String pomPattern = ".*pom.xml.*";
        String dotPattern = "\\..*";
        assert fs != null;
        for (File f : fs){
            //System.out.println(f.getPath());
            if (f.isDirectory() && !Pattern.matches(dotPattern, f.getName()) && !Pattern.matches(srcPattern, f.getName())){
                generateXmlPath(f);
            }
            if (Pattern.matches(pomPattern, f.getName())){
                //System.out.println(f.getPath());
                xmlPathList.add(f.getPath());
            }
        }
        return xmlPathList;
    }

    private static void generatePropertiesMap(Document doc){
        if(doc.getElementsByTagName("properties").getLength() == 0) return;
        NodeList nl = doc.getElementsByTagName("properties").item(0).getChildNodes();
        System.out.println(doc.getElementsByTagName("properties").item(0).getChildNodes().getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            if(nl.item(i) instanceof Element){
                Element node = (Element)nl.item(i);
                //以下两行区别 参考https://stackoverflow.com/questions/22683699/getnodename-not-getting-node-name 第二个回答
                propertiesMap.put(node.getNodeName(), node.getFirstChild().getNodeValue());

//                System.out.println(node.getNodeName());
//                System.out.println(node.getFirstChild().getNodeValue());
            }

        }
        return;
    }

    private static String mapProperties(String s){
        String propertyPattern = "\\$\\{.*}";
        if(Pattern.matches(propertyPattern, s)){
            String property = s.substring(2, s.length() - 1);
            return propertiesMap.get(property);
        }
        return s;
    }

    public Project parseXml(String path){
        System.out.println("解析" + path);
        Project project = new Project();
        project.setPomPath(path);
        try {
            File f = new File(path);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);
            //生成properties常量map
            generatePropertiesMap(doc);

            //解析结点本身信息和父节点信息
            project.setParent(new Project());
            Element root = doc.getDocumentElement();
            boolean pro = false , par = false;
            //保险起见，三种标签分开循环处理
            //groupId
            NodeList groupId = root.getElementsByTagName("groupId");
            for(int i = 0; i < groupId.getLength(); i++){
                Element element = (Element) groupId.item(i);
                if("project".equals(element.getParentNode().getNodeName())){
                    project.setGroupId(element.getFirstChild().getNodeValue());
                    pro = true;
                }
                if("parent".equals(element.getParentNode().getNodeName())){
                    project.getParent().setGroupId(element.getFirstChild().getNodeValue());
                    par = true;
                }
                if(pro && par){
                    pro = false; par = false;
                    break;
                }
            }
            //artifactId
            NodeList artifactId = root.getElementsByTagName("artifactId");
            for(int i = 0; i < artifactId.getLength(); i++){
                Element element = (Element) artifactId.item(i);
                if("project".equals(element.getParentNode().getNodeName())){
                    project.setArtifactId(element.getFirstChild().getNodeValue());
                    pro = true;
                }
                if("parent".equals(element.getParentNode().getNodeName())){
                    project.getParent().setArtifactId(element.getFirstChild().getNodeValue());
                    par = true;
                }
                if(pro && par){
                    pro = false; par = false;
                    break;
                }
            }
            //version
            NodeList version = root.getElementsByTagName("version");
            for(int i = 0; i < version.getLength(); i++){
                Element element = (Element) version.item(i);
                if("project".equals(element.getParentNode().getNodeName())){
                    project.setVersion(mapProperties(element.getFirstChild().getNodeValue()));
                    //project.version常量需要加到propertiesMap
                    propertiesMap.put("project.version", element.getFirstChild().getNodeValue());
                    pro = true;
                }
                if("parent".equals(element.getParentNode().getNodeName())){
                    project.getParent().setVersion(mapProperties(element.getFirstChild().getNodeValue()));
                    par = true;
                }
                if(pro && par){
                    pro = false; par = false;
                    break;
                }
            }

            //build
            if (doc.getElementsByTagName("build").getLength() != 0){
                project.setBuild(doc.getElementsByTagName("build").item(0).getTextContent());
            }else{

            }

            //modules
            project.setModules(new ArrayList<>());
            NodeList modules = doc.getElementsByTagName("module");
            for(int i = 0; i < modules.getLength(); i++){
                Element module = (Element)modules.item(i);
                if("modules".equals(module.getParentNode().getNodeName())){
                    project.getModules().add(module.getFirstChild().getNodeValue());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }




}
