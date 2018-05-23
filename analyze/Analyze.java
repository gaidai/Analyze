package analyze;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author S.Gaidai
 */

public class Analyze {
    Node originalNode;
    Node closestAnalog;
    int maxPoints = 0;
    Document original;
    Document sample;
    DocumentBuilder documentBuilder;
    XPathFactory pathFactory;
    XPath xpath ;
    List <Node> keys = new ArrayList();
    List <Node> candidates = new ArrayList();
    
    public static void main(String[] args) {
        
           Analyze obj = new Analyze();
           obj.analyzation(args);
    }    
    
    public void analyzation(String[] args)   {  
        
        parseOriginal(args);
        
        for(Node attr: keys){
            String selector = "//*[@"+attr.getNodeName()+"='"+attr.getNodeValue()+"']";            
            getAnalogNodes(selector);            
        }        
        if(!candidates.isEmpty()){
            // Compare origin tag attrs with each of candidate nodes
            for(Node candidate : candidates){
                int currentPoints = 0;
                String currentAttrValue = null;
                for(Node key: keys){
                    if(candidate.getAttributes().getNamedItem(key.getNodeName()) != null){
                        currentAttrValue = candidate.getAttributes().getNamedItem(key.getNodeName()).getNodeValue();
                        if(candidate.getAttributes().getNamedItem(key.getNodeName()).equals("id") ){
                            if(currentAttrValue != null && currentAttrValue.equals(key.getNodeValue())){
                             currentPoints = currentPoints +20;
                                System.out.println(" Detected match by id");
                            }
                        }else{
                            if(currentAttrValue != null && currentAttrValue.equals(key.getNodeValue())){
                                currentPoints++;
                            }
                        }
                    }                    
                }
                if(candidate.getTextContent().trim().equals(originalNode.getTextContent().trim())){
                    currentPoints++;
                }
                if(candidate.getNodeName().equals(originalNode.getNodeName())){
                    currentPoints++;
                }
                //compare matches with closestAnalog 
                if(currentPoints > maxPoints){
                    maxPoints = currentPoints;
                    closestAnalog = candidate;
                }                
            }   
        }
        
        if (closestAnalog != null){
            printFullPath(closestAnalog);
        }else{
            System.out.println();
            System.out.println("Any results");
        }                
    }
    
    public  void parseOriginal(String[] args) {   
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            original = documentBuilder.parse(args[0]);
            sample = documentBuilder.parse(args[1]);
            pathFactory = XPathFactory.newInstance();
            xpath = pathFactory.newXPath();
            // Change argument for another search
            XPathExpression expr = xpath.compile("//*[@id='make-everything-ok-button']");
            NodeList nodes = (NodeList) expr.evaluate(original, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                NamedNodeMap  attrs =  n.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    keys.add(attrs.item(j));
                };
                originalNode = n;
            }  
           
        }catch(ParserConfigurationException |XPathExpressionException | SAXException | IOException ex) {
            System.out.println("Parsing error !");
            ex.printStackTrace(System.out);
        }
    }    
    // Fined Nodes from Sample page with analog attrs
    public void getAnalogNodes(String selector) {        
        try{
            XPathExpression expr = xpath.compile(selector);
            NodeList nodes = (NodeList) expr.evaluate(sample, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                candidates.add(nodes.item(i));
            }  
         }catch(XPathExpressionException ex) {
            System.out.println("Parsing error in Sample File !");
            ex.printStackTrace(System.out);
        }
    }   
    public void printFullPath(Node n){
        String path =  n.getNodeName();
        n =  n.getParentNode();
        while (n != null ) {
            int num = 0;            
            Node prev = n.getPreviousSibling();
            while (prev.getPreviousSibling() != null ){
                if(n.getNodeName().equals(prev.getNodeName())){
                    num++;
                }
                prev = prev.getPreviousSibling();
            }
            if(num>0){
                path = n.getNodeName() + "["+num+"]"+" > " + path;
            }else{
                path = n.getNodeName() + " > " + path;
            }
            if ( n.getNodeName().trim().equals("body")){
                path = "html > " + path;
                break;
            }
            n = n.getParentNode();
        }
        System.out.println();
        System.out.println( "Path : " +path + "  {" +maxPoints +" analog features}" );
        System.out.println();
    }
 
}
