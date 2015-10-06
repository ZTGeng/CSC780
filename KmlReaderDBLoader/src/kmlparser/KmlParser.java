/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kmlparser;

/**
 *
 * @author xuejing
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.sql.*;
public class KmlParser {
   
    public static void main(String[] args) throws ParserConfigurationException,
    SAXException, IOException{
        //if(args.length == 0) return;
        //String db = args[0];
        Connection c = null;
        Statement stmt = null;
        
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:StreetSweepDB");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String[] test = {};
            String[] test1 = {"StreetSweep_01.kml","StreetSweep_02.kml","StreetSweep_03.kml", "StreetSweep_04.kml","StreetSweep_05.kml","StreetSweep_06.kml",
            "StreetSweep_07.kml","StreetSweep_08.kml","StreetSweep_09.kml","StreetSweep_10.kml","StreetSweep_11.kml"};
            for(int input = 0; input < test.length; input++) {
                Document document = builder.parse(new File(test[input]));
                List<String> name= new ArrayList<>();
                NodeList nodeList = document.getDocumentElement().getChildNodes();
            
                stmt = c.createStatement();
                
                //get Document layer
                Node node = nodeList.item(1);
            
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    String nm = elem.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                    //System.out.println(nm);
                
                    //get Placemark layer, the db insertion starts here
                    NodeList pmList = elem.getElementsByTagName("Placemark");
                
                    for(int j = 0; j < pmList.getLength(); j++) {
                        String sql = "INSERT INTO StreetSweepData VALUES (";
                        Node pmNode = pmList.item(j);
                        if(pmNode.getNodeType() == Node.ELEMENT_NODE){
                            Element pmElem = (Element)pmNode;
                            String id = pmElem.getAttribute("id");
                            System.out.println(id);
                            sql = sql + null + ",";
                        
                            String pm_info = pmElem.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                            //System.out.println(pm_info);
                            sql = sql + "\"" + pm_info + "\"" + ", ";
                        
                            //Schema data layer(most placemark has 22 entries, few have only 21)
                            NodeList simpleList = pmElem.getElementsByTagName("SimpleData");
                        
                            for(int k = 0; k < simpleList.getLength(); k++) {
                                Node dataNode = simpleList.item(k);
                                if(dataNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element dataElem = (Element) dataNode;
                                    String attr_name = dataElem.getAttribute("name");
                                    if(k==2 && !attr_name.equals("BlockSide")) {
                                        sql = sql+ "\"" + " "+ "\"" + ", ";
                                    }
                                    String attr_val = dataNode.getChildNodes().item(0).getNodeValue();
                                    //System.out.println(attr_name + ": " + attr_val);
                                    sql = sql+ "\"" + attr_val + "\"" + ", ";
                                }
                            }
                            //Coordinates
                            String coordinates = pmElem.getElementsByTagName("coordinates").item(0).getChildNodes().item(0).getNodeValue();
                            //System.out.println("Coordinates: " + coordinates);
                            sql = sql+ "\"" + coordinates + "\"" +");";
               
                        }
                        //System.out.println(sql);
                        stmt.executeUpdate(sql);        
                    }
                    
                }
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
            System.out.println("Records created successfully");
        
    }
    
}