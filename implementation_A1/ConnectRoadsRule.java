/**
 * Connects disconnected road segments if they're within 2 units and helps build
 * longer roads for the longest road
 */
public class ConnectRoadsRule implements Rule {

    @Override
    public double evaluate(Player player, Board board, Resources resources) {
        if (!canAffordRoad(player)) return 0.0;

        if (findConnectingEdge(player, board) != null) {
            return 9.0; // High priority constraint
        }

        return 0.0;
    }

    @Override
    public Command createCommand(Player player, Board board, Resources resources) {
        if (!canAffordRoad(player)) return null;

        Edge connecting = findConnectingEdge(player, board);
        if (connecting != null) {
            return new BuildRoad(
                    connecting.getIntersection1().getNodeID(),
                    connecting.getIntersection2().getNodeID(),
                    board, resources, player
            );
        }

        return null;
    }

    @Override
    public String getName() { return "ConnectRoads"; }

    private boolean canAffordRoad(Player p) {
        return p.getResourceCount(Resource.BRICK) >= 1
                && p.getResourceCount(Resource.LUMBER) >= 1;
    }

    /**
     * Finds an empty edge that connects two of the players road segments
     */
    private Edge findConnectingEdge(Player p, Board board) {
        for (Edge empty : board.getEdges()) {
            if (empty.getOwner() != null) continue;

            Intersection a = empty.getIntersection1();
            Intersection b = empty.getIntersection2();

            // Check if both ends touch player's roads
            boolean aConnected = hasPlayerRoadNearby(p, a);
            boolean bConnected = hasPlayerRoadNearby(p, b);

            if (aConnected && bConnected) {
                return empty; // This edge connects two segments
            }
        }
        return null;
    }

    private boolean hasPlayerRoadNearby(Player p, Intersection intersection) {
        for (Edge e : intersection.getEdges()) {
            if (p.equals(e.getOwner())) return true;
        }
        return false;
    }
}