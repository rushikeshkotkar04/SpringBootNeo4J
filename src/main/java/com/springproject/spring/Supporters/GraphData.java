package com.springproject.spring.Supporters;


import java.util.List;
import java.util.Map;

public class GraphData {
    private List<Map<String, Object>> nodesWithRelations;
    private List<Map<String, Object>> nodesWithoutRelations;
    private List<Map<String, Object>> edges;

    public List<Map<String, Object>> getNodesWithRelations() {
        return nodesWithRelations;
    }

    public void setNodesWithRelations(List<Map<String, Object>> nodesWithRelations) {
        this.nodesWithRelations = nodesWithRelations;
    }

    public List<Map<String, Object>> getNodesWithoutRelations() {
        return nodesWithoutRelations;
    }

    public void setNodesWithoutRelations(List<Map<String, Object>> nodesWithoutRelations) {
        this.nodesWithoutRelations = nodesWithoutRelations;
    }

    public List<Map<String, Object>> getEdges() {
        return edges;
    }

    public void setEdges(List<Map<String, Object>> edges) {
        this.edges = edges;
    }
}
