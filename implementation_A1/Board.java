import java.util.ArrayList;
import java.util.List;

/**
 * Board class implements a blueprint of the map where the Catan game will be implemented.
 * We also put other methods that the board should be responsible for to reduce dependency
 * (information specialist) such as building roads, settlements and cities as well as checking
 * if the edges are connected to the players.
 */

public class Board {
    // Creating the instances needed for the board such as the lists of tiles, intersections and edges
    private List<Tile> tiles = new ArrayList<>();
    private List<Intersection> intersections = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    //Introducing the robberTile that will track the robber position within the tiles
    private Tile robberTile;

    /**
     * Board constructor to create a map where the CATAN game will be hosted
     * We will use the Board config class to maintain SRP when creating the
     * new board.
     */
    public Board() {
        BoardConfig.buildMap(tiles, intersections, edges);

        //Assigning the terrain desert to the robber tile through a for loop over all the tiles
        for (Tile t : tiles) {
            if (t.getTerrain() == Terrain.DESERT) {
                robberTile = t;
                break;
            }
        }
    }

    // Getter method to return the list of tiles per token
    public List<Tile> getTilesByToken(int token) {
        List<Tile> result = new ArrayList<>();
        for (Tile t : this.tiles) {
            if (t.getToken() == token) {
                result.add(t);
            }
        }
        return result;
    }

    // Getter method to return the list of Tiles
    public List<Tile> getTiles() {
        return List.copyOf(tiles);
    }

    // Getter method to return the list of Intersections
    public List<Intersection> getIntersections() {
        return List.copyOf(intersections);
    }

    // Getter method to return the list of Edges
    public List<Edge> getEdges() {
        return List.copyOf(edges);
    }

    /**
     * buildRoad returns the boolean value reflecting the success of building the road in the edge passed as param.
     * @return true if built successfully
     */
    public boolean buildRoad(Player p, Edge e) {
        // Checking if the edge has an owner already
        if (e.getOwner() != null) return false;

        // Checking if the edge is connected to player
        boolean connected = isEdgeConnectedToPlayer(p, e);
        if (!connected) return false;

        // Setting the owner to the edge
        e.setOwner(p);
        p.recordRoadBuilt(e.getEdgeID());
        return true;
    }

    /**
     * buildSettlement, if no 3rd parameter is passed, we will instantiate it as if the initial placement is false
     */
    public boolean buildSettlement(Player p, Intersection i) {
        return buildSettlement(p, i, false);
    }

    /**
     * build method used to build the settlement to the player
     * @param isInitialPlacement if true, we skip the "must connect to road" rule.
     */
    public boolean buildSettlement(Player p, Intersection i, boolean isInitialPlacement) {
        if (i.getOwner() != null) return false;

        // distance rule: adjacent intersections must be vacant
        for (Intersection neighbor : i.getAdjacentIntersections()) {
            if (neighbor.getOwner() != null) {
                return false;
            }
        }

        // connection rule (skip if initial placement)
        if (!isInitialPlacement) {
            boolean hasPlayerRoad = false;
            for (Edge e : i.getEdges()) {
                if (p.equals(e.getOwner())) {
                    hasPlayerRoad = true;
                    break;
                }
            }
            if (!hasPlayerRoad) return false;
        }

        i.setOwner(p);
        i.setCity(false);
        p.recordSettlementBuilt(i.getNodeID());
        return true;
    }

    /**
     * buildCity used to build city in the intersection but we need to account for the fact that a city
     * already is built there, or if the intersection has an owner already
     */
    public boolean buildCity(Player p, Intersection i) {
        // Checking if intersection has already an owner
        if (!p.equals(i.getOwner())) return false;

        //Checking if the intersection is already a city
        if (i.isCity()) return false;

        i.setCity(true);
        p.recordCityBuilt();
        return true;
    }

    // Checking if the edge is connected to player through different checks
    private boolean isEdgeConnectedToPlayer(Player p, Edge e) {
        Intersection a = e.getIntersection1();
        Intersection b = e.getIntersection2();

        // Connected if endpoint is player's settlement/city
        if (p.equals(a.getOwner()) || p.equals(b.getOwner())) {
            return true;
        }

        // Connected if endpoint touches another player-owned road
        for (Edge ea : a.getEdges()) {
            if (p.equals(ea.getOwner())) return true;
        }
        for (Edge eb : b.getEdges()) {
            if (p.equals(eb.getOwner())) return true;
        }
        return false;
    }

    /**
     * Getter method to return the tile where the robber is at
     * @return the tile where the robber is at
     */
    public Tile getRobberTile(){
        return robberTile;
    }

    /**
     * Setter method that is used to set the robber in a tile
     * @param robberTile teh tile where the robber is at
     */
    public void setRobberTile(Tile robberTile) {
        this.robberTile = robberTile;
    }
}
