import java.util.ArrayList;
import java.util.List;

/**
 * Intersection class is finale so no changes are implemented to it that may cause impact on it.
 * The intersections are defined by their unique ID's. We added getters and setters to modify
 * the owner of the intersection, the list of intersections adjacent to the edge, and to check
 * if a city exists there or not.
 */
public final class Intersection {
    private int nodeID;
    private List<Edge> edges = new ArrayList<>();
    private List<Tile> tiles = new ArrayList<>();
    private Player owner;
    private boolean isCity;

    /**
     * The intersection is defined by its own unique ID
     * @param nodeID The ID of the intersection
     */
    Intersection(int nodeID) {
        this.nodeID = nodeID;
    }
    // Getter method to return the ID of the intersection
    int getNodeID() {
        return nodeID;
    }
    // Setter method to set the id of the intersection
    void setNodeID(int id) {
        this.nodeID = id;
    }
    // Getter method to return the list of edges of the intersection
    List<Edge> getEdges() {
        return edges;
    }
    // Adder method to add the edges passed along in the method
    void addEdge(Edge e) {
        edges.add(e);
    }
    // Adder method to add the tiles passed along in the method
    void addTile(Tile t) {
        tiles.add(t);
    }
    // Getter method to return the list of tiles surrounding the interserction
    List<Tile> getTiles() {
        return tiles;
    }
    // Getter method returning the player that owns the inetrsection
    Player getOwner() {
        return owner;
    }
    // Setter method to set the owner of the intersection
    void setOwner(Player owner) {
        this.owner = owner;
    }
    // isCity method returns true if the intersection is a city, false if not
    boolean isCity() {
        return isCity;
    }
    // Setter method to set the city if it exists in that spot
    void setCity(boolean city) {
        isCity = city;
    }

    // Getter method to return the list of intersections connected to an edge
    List<Intersection> getAdjacentIntersections() {
        List<Intersection> intersections = new ArrayList<>();
        //Looping over each edge and checking if the intersections is matching, it gets added to the list intersections
        for (Edge edge : edges) {
            Intersection other = (edge.getIntersection1() == this) ? edge.getIntersection2() : edge.getIntersection1();
            intersections.add(other);
        }
        return intersections;
    }
}