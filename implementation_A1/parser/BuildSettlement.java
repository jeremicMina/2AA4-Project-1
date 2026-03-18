package parser;

/**
 * BuildSettlement represents the action of placing a settlement on the board at an intersection node
 * command pattern implementation:
 *      - execute(): places the settlement on the board, deducts resources from the bank
 *      - undo(): removes the settlement, returns resources to the bank
 *
 * the command stores everything it needs at construction time (board, resources, player)
 */

public class BuildSettlement implements Command{

    //data from parser
    private int nodeId;
    //context injected so execute() and undo() can act on game state
    private final Board board;
    private final Resources resources;
    private final Player player;

    //snapshot stored during execute() so undo() knows what to reserve
    //the actual Intersection object that was built on, it is set when execute() succeeds
    private Intersection builtOn;

    /**
     * constructor for a BuildSettlement command
     *
     * @param nodeId    the board intersection ID where the settlement will be placed
     * @param board     the game board used to find the intersection and call buildSettlement
     * @param resources the resource bank used to deduct and return costs
     * @param player    the player performing the action
     */
    public BuildSettlement(int nodeId, Board board, Resources resources, Player player) {
        this.nodeId = nodeId;
        this.board = board;
        this.resources = resources;
        this.player = player;
    }

    /**
     * returns the node ID this settlement targets
     * used by Demonstrator to log which node was acted on
     */
    public int getNodeId() { return nodeId; }

    /**
     * executes the build settlement action:
     * 1. finds the intersection on board by nodeId
     * 2. deducts the settlement cost from bank (1 BRICK, 1 LUMBER, 1 WOOL, 1 GRAIN)
     * 3. places settlement through board.buildSettlement()
     * 4. stores intersection in builtOn so undo() can reverse it
     *
     * if intersection isn't found or the build fails, command does nothing
     * and builtOn stays null — undo() checks for this and safely skips
     */
    @Override
    public void execute() {
        // find the intersection matching the nodeId on board
        Intersection target = findIntersection();
        if (target == null) {return;} //if intersection fails, return

        // deduct cost from bank. one of each: BRICK, LUMBER, WOOL, GRAIN
        boolean paid = resources.spendResources(1, player, Resource.BRICK)
                && resources.spendResources(1, player, Resource.LUMBER)
                && resources.spendResources(1, player, Resource.WOOL)
                && resources.spendResources(1, player, Resource.GRAIN);

        if (!paid) {return;} //if failure in payment, return

        // tries to place the settlement and enforces distance and road rules
        boolean built = board.buildSettlement(player, target);

        if (built) {
            // if build successful; store the intersection so undo() knows what to reverse
            builtOn = target;
        } else {
            // if build failed; give back the resources that were already deducted
            resources.giveResources(1, player, Resource.BRICK);
            resources.giveResources(1, player, Resource.LUMBER);
            resources.giveResources(1, player, Resource.WOOL);
            resources.giveResources(1, player, Resource.GRAIN);
        }
    }

    /**
     * reverses the build settlement action:
     * 1. checks builtOn and if null then execute() never succeeded so nothing to reverse
     * 2. removes settlement by clearing the intersection owner
     * 3. removes record from the player's settlement list
     * 4. returns cost back to the bank (1 BRICK, 1 LUMBER, 1 WOOL, 1 GRAIN)
     * 5. resets builtOn to null so command cannot be undone twice
     */
    @Override
    public void undo() {
        // if execute() never succeeded then nothing to undo
        if (builtOn == null) {return;}

        //remove the settlement from the intersection
        builtOn.setOwner(null);
        builtOn.setCity(false);

        //return the resources to the bank
        resources.giveResources(1, player, Resource.BRICK);
        resources.giveResources(1, player, Resource.LUMBER);
        resources.giveResources(1, player, Resource.WOOL);
        resources.giveResources(1, player, Resource.GRAIN);

        //clear the snapshot so this command cannot be undone twice
        builtOn = null;
    }

    /**
     * walks the board's intersection list to find the matching nodeId
     * returns null if no intersection has that ID
     */
    private Intersection findIntersection() {
        for (Intersection i : board.getIntersections()) {
            if (i.getNodeID() == nodeId) return i;
        }
        return null;
    }


    @Override
    public String name() { return "BUILD_SETTLEMENT"; }

    @Override
    public String toString() { return "BUILD_SETTLEMENT " + nodeId; }
}
