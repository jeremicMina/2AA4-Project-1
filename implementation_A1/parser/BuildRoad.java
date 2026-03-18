package parser;

/**
 * BuildRoad represents the action of placing a road on board between two intersection nodes
 *
 * command pattern implementation:
 *      - execute(): finds the edge, deducts road cost, places the road
 *      - undo(): removes the road, returns resources to the bank
 *
 * the command stores the board, resources, and player at construction time so it is reversible
 */

public class BuildRoad implements Command{

    //data from parser
    private int fromNodeId;
    private int toNodeId;

    //context injected so execute() and undo() can act on game state
    private final Board board;
    private final Resources resources;
    private final Player player;

    //snapshot stored during execute() so undo() knows what to reserve
    //the actual Intersection object that was built on, it is set when execute() succeeds
    private Edge builtOn;

    /**
     * constructor for a BuildRoad command
     *
     * @param fromNodeId the node ID of first intersection endpoint
     * @param toNodeId   the node ID of second intersection endpoint
     * @param board      the game board used to find the edge and call buildRoad
     * @param resources  the resource bank used to deduct and return costs
     * @param player     the player performing the action
     */
    public BuildRoad(int fromNodeId, int toNodeId, Board board, Resources resources, Player player) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.board = board;
        this.resources = resources;
        this.player = player;
    }

    /**
     * Returns the FromNode ID this road starts at
     */
    public int getFromNodeId() { return fromNodeId; }

    /**
     * Returns the ToNode ID this road starts at
     */
    public int getToNodeId() { return toNodeId; }

    /**
     * executes the build road action:
     * 1. finds the edge connecting fromNodeId and toNodeId on the board
     * 2. deducts the road cost from the bank (1 BRICK, 1 LUMBER)
     * 3. places the road through the board.buildRoad() method
     * 4. stores the edge in builtOn so undo() can reverse it
     *
     * if the edge is not found or the build fails the command does nothing
     * and builtOn stays null so undo() checks for this and safely skips
     */
    @Override
    public void execute() {
        // find the edge on the board that connects fromNodeId and toNodeId
        Edge target = findEdge();
        if (target == null) return;

        // deduct cost from bank : 1 BRICK + 1 LUMBER
        boolean paid = resources.spendResources(1, player, Resource.BRICK)
                && resources.spendResources(1, player, Resource.LUMBER);

        if (!paid) return;

        // tries to place the road and enforces connection and ownership rules
        boolean built = board.buildRoad(player, target);

        if (built) {
            // store the edge so undo() knows what to reverse
            builtOn = target;
        } else {
            // if build failed we refund the resources to the player
            resources.giveResources(1, player, Resource.BRICK);
            resources.giveResources(1, player, Resource.LUMBER);
        }
    }

    /**
     * walks the board's edge list to find the edge with the two endpoints
     * match fromNodeId and toNodeId in either order and returns null if it doesn't exist
     */
    private Edge findEdge() {
        for (Edge e : board.getEdges()) {
            int id1 = e.getIntersection1().getNodeID();
            int id2 = e.getIntersection2().getNodeID();

            //check both directions since edges are undirected
            if ((id1==fromNodeId&&id2==toNodeId) || (id1==toNodeId&&id2==fromNodeId)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public String name() { return "BUILD_ROAD"; }

    @Override
    public String toString() { return "BUILD_ROAD " + fromNodeId + " " + toNodeId; }
}
