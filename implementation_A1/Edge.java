/**
 * Edge class is finale so no changes are implemented to it that may cause impact on it.
 * The edge class keeps record of the ID, the owner of the edge, and both intersections
 * that cut through the edge.
 */
public final class Edge {
    private int edgeID;
    private Player owner;
    public Intersection intersection1;
    public Intersection intersection2;

    /**
     * Edge constructor using the ID for reference of it, as well as both intersections
     * that cut through it.
     * @param edgeID int ID of each edge to keep track of them
     * @param intersection1 First intersection touching the edge
     * @param intersection2 Second intersection touching the edge from the other side
     */
    Edge(int edgeID, Intersection intersection1, Intersection intersection2) {
        this.edgeID = edgeID;
        this.intersection1 = intersection1;
        this.intersection2 = intersection2;
    }
    // Getter method to return the Edge ID
    int getEdgeID() {
        return edgeID;
    }
    // Setter method to set the Edge ID
    void setEdgeID(int id) {
        this.edgeID = id;
    }
    // Getter method to return the Edge owner
    Player getOwner() {
        return owner;
    }
    // Setter method to set the Edge owner player
    void setOwner(Player owner) {
        this.owner = owner;
    }
    // Getter method to return the Edge first intersection
    public Intersection getIntersection1() {
        return intersection1;
    }
    // Getter method to return the Edge second intersection
    public Intersection getIntersection2() {
        return intersection2;
    }
}