package com.springproject.spring.controller;

import com.springproject.spring.Supporters.GraphData;
import com.springproject.spring.config.Neo4jConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.neo4j.driver.*;

import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.Values.parameters;

@Controller
public class Practice {

//    @Value("${neo4j.uri}")
//    private String uri="bolt://localhost:7687";
    private String uri="bolt+s://4e4cd635.databases.neo4j.io:7687";


    //    @Value("${neo4j.username}")
    private String username="neo4j";

//    @Value("${neo4j.password}")
//    private String password="Rushi@10";
    private String password="hEXugDSlkci6RLpHIUuIF91Swa1gsIi9RgsyjSsniw0";
    Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));




    //    private final Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    @RequestMapping("/")
    public String home(){
        return "home.html";
    }

    @RequestMapping("/add-node")
    public String addNode(HttpServletRequest request, Model model){

        String name=request.getParameter("node-name");
        System.out.println(name);
        try (Session session=driver.session()){
            String cypherQuery = "CREATE (p:Person {name: $name}) RETURN p";
            session.run(cypherQuery, parameters("name", name));
        }

//        model.addAttribute("node",node);
        return "redirect:/";
    }
    @RequestMapping("/add-relationship")
    public String addRealationship(HttpServletRequest request, Model model){
        String relationship=request.getParameter("relationship-name");
        String person1=request.getParameter("person1");
        String person2=request.getParameter("person2");
        try (Session session=driver.session()){
            String cypherQuery = "MATCH (a:Person {name: $person1}), (b:Person {name: $person2}) " +
                    "MERGE (a)-[:FRIEND]->(b) RETURN a, b";
            session.run(cypherQuery, parameters("person1", person1, "person2", person2));
        }

        model.addAttribute("node",relationship);
        return "redirect:/";
    }

    @RequestMapping("/show")
    public String show(Model model){
        GraphData graphData = new GraphData();

        // Creating sets to avoid duplicate nodes
        Map<Long, Map<String, Object>> nodeMapWithRelations = new HashMap<>();
        Map<Long, Map<String, Object>> nodeMapWithoutRelations = new HashMap<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        try (Session session = driver.session()) {
            // Query to get nodes with relationships
            String queryWithRelations = "MATCH (n)-[r]->(m) RETURN n, r, m";
            Result resultWithRelations = session.run(queryWithRelations);

            // Process the result for nodes with relationships
            while (resultWithRelations.hasNext()) {
                Record record = resultWithRelations.next();

                InternalNode node1 = record.get("n") != null ? (InternalNode) record.get("n").asNode() : null;
                InternalNode node2 = null;

                if (record.get("m") != null) {
                    node2 = (InternalNode) record.get("m").asNode();
                    // Add node2 to the nodeMapWithRelations if it's not already present
                    if (!nodeMapWithRelations.containsKey(node2.id())) {
                        nodeMapWithRelations.put(node2.id(), Map.of("id", node2.id(), "label", node2.get("name").asString()));
                    }
                }

                // Add node1 to the nodeMapWithRelations if it's not already present
                if (!nodeMapWithRelations.containsKey(node1.id())) {
                    nodeMapWithRelations.put(node1.id(), Map.of("id", node1.id(), "label", node1.get("name").asString()));
                }

                // If there's a relationship, create an edge
                if (record.get("r") != null) {
                    InternalRelationship relationship = (InternalRelationship) record.get("r").asRelationship();
                    if (node2 != null) {
                        edges.add(Map.of("source", node1.id(), "target", node2.id(), "label", relationship.type()));
                    } else {
                        // If node2 is null, it's a self-loop (relationship to itself)
                        edges.add(Map.of("source", node1.id(), "target", node1.id(), "label", relationship.type()));
                    }
                }
            }

            // Query to get nodes without relationships (nodes that don't have any relationships)
            String queryWithoutRelations = "MATCH (n) WHERE NOT (n)-[]->() RETURN n";
            Result resultWithoutRelations = session.run(queryWithoutRelations);

            // Process the result for nodes without relationships
            while (resultWithoutRelations.hasNext()) {
                Record record = resultWithoutRelations.next();

                InternalNode node = record.get("n") != null ? (InternalNode) record.get("n").asNode() : null;

                if (node != null) {
                    nodeMapWithoutRelations.put(node.id(), Map.of("id", node.id(), "label", node.get("name").asString()));
                }
            }

            // Convert maps to lists
            List<Map<String, Object>> nodesWithRelations = new ArrayList<>(nodeMapWithRelations.values());
            List<Map<String, Object>> nodesWithoutRelations = new ArrayList<>(nodeMapWithoutRelations.values());

            // Set the graph data to the model
            graphData.setNodesWithRelations(nodesWithRelations);
            graphData.setNodesWithoutRelations(nodesWithoutRelations);
            graphData.setEdges(edges);
        }

        // Add the graph data to the model for Thymeleaf rendering
        model.addAttribute("graphData", graphData);


        return "node";
    }


}


