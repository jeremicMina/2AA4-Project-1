package parser;

/**
 * BuildCity represents the action of upgrading a settlement to a city at an intersection node
 *
 * command pattern implementation:
 *      - execute(): finds the intersection, deducts city cost, upgrades settlement to city
 *      - undo(): downgrades the city back to a settlement, returns resources to the bank
 *
 * BuildCity is a simple undo() because upgrading and downgrading
 * is just flipping the isCity flag on the intersection
 */

public class BuildCity implements Command{

    //data from parser
    private int nodeId;

    //context injected so execute() and undo() can act on game state
    private final Board board;
    private final Resources resources;
    private final Player player;

    //snapshot stored during execute() so undo() knows what to reserve
    //the actual Intersection object that was upgraded on, it is set when execute() succeeds
    private Intersection upgradedAt;

    /**
     * constructor for a BuildCity command
     *
     * @param nodeId the board ID where city will be upgraded in
     * @param board      the game board used to find the edge and call buildRoad
     * @param resources  the resource bank used to deduct and return costs
     * @param player     the player performing the action
     */
    public BuildCity(int nodeId, Board board, Resources resources, Player player) {
        this.nodeId = nodeId;
        this.board = board;
        this.resources = resources;
        this.player = player;
    }

    /**
     * returns the node ID this city targets
     */
    public int getNodeId() { return nodeId; }

    /**
     * executes the build city action:
     * 1. finds the intersection on board
     * 2. deducts the city cost from bank (3 ORE, 2 GRAIN)
     * 3. upgrades settlement to a city through board.buildCity()
     * 4. stores intersection in upgradedAt so undo() can reverse it
     *
     * if intersection not found, player doesn't own it, or resources are insufficient
     * command does nothing and upgradedAt stays null
     */
    @Override
    public void execute() {
        // find the intersection on board matching nodeId
        Intersection target = findIntersection();
        if (target == null) return;

        // deduct cost from bank — city costs 3 ORE + 2 GRAIN
        boolean paid = resources.spendResources(3, player, Resource.ORE)
                && resources.spendResources(2, player, Resource.GRAIN);

        if (!paid) {return;}

        // attempt to upgrade to city, board.buildCity() checks player owns a settlement here
        boolean built = board.buildCity(player, target);

        if (built) {
            //store the intersection so undo() knows what to reverse
            upgradedAt = target;
        } else {
            // if upgrade failed; refund the resources to player
            resources.giveResources(3, player, Resource.ORE);
            resources.giveResources(2, player, Resource.GRAIN);
        }
    }

    /**
     * reverses the build city action:
     * 1. checks upgradedAt and if null, execute() never succeeded so nothing to reverse
     * 2. downgrades the city back to a settlement by setting isCity to false
     * 3. returns cost back to the bank (3 ORE, 2 GRAIN)
     * 4. resets upgradedAt to null so this command can't be undone twice
     */
    @Override
    public void undo() {
        //if execute() doesn't succeeded, nothing to undo
        if (upgradedAt == null) return;

        //downgrade city back to settlement by flipping the isCity flag
        upgradedAt.setCity(false);

        //return the resources to bank
        resources.giveResources(3, player, Resource.ORE);
        resources.giveResources(2, player, Resource.GRAIN);

        //clear the snapshot so the same command can't be undone twice
        upgradedAt = null;
    }

    /**
     * walks the board's intersection list to find the matching nodeId and returns null if not found
     */
    private Intersection findIntersection() {
        for (Intersection i : board.getIntersections()) {
            if (i.getNodeID() == nodeId) return i;
        }
        return null;
    }

    @Override
    public String name() { return "BUILD_CITY"; }

    @Override
    public String toString() { return "BUILD_CITY " + nodeId; }
}
