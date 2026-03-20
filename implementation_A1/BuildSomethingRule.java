/**
 * Building without earning VP is worth 0.8.
 */
public class BuildSomethingRule implements Rule {

    @Override
    public double evaluate(Player player, Board board, Resources resources) {
        if (canAffordRoad(player) && findValidRoadLocation(player, board) != null) {
            return 0.8;
        }
        return 0.0;
    }

    @Override
    public Command createCommand(Player player, Board board, Resources resources) {
        if (canAffordRoad(player)) {
            Edge edge = findValidRoadLocation(player, board);
            if (edge != null) {
                return new BuildRoad(
                        edge.getIntersection1().getNodeID(),
                        edge.getIntersection2().getNodeID(),
                        board, resources, player
                );
            }
        }
        return null;
    }

    @Override
    public String getName() { return "BuildSomething"; }

    private boolean canAffordRoad(Player p) {
        return p.getResourceCount(Resource.BRICK) >= 1
                && p.getResourceCount(Resource.LUMBER) >= 1;
    }

    private Edge findValidRoadLocation(Player p, Board board) {
        for (Edge e : board.getEdges()) {
            if (e.getOwner() == null && isConnectedToPlayer(p, e)) {
                return e;
            }
        }
        return null;
    }

    private boolean isConnectedToPlayer(Player p, Edge e) {
        Intersection a = e.getIntersection1();
        Intersection b = e.getIntersection2();

        // Edge touches player's settlement
        if (p.equals(a.getOwner()) || p.equals(b.getOwner())) return true;

        // Edge connects to player's road
        for (Edge ea : a.getEdges()) {
            if (p.equals(ea.getOwner())) return true;
        }
        for (Edge eb : b.getEdges()) {
            if (p.equals(eb.getOwner())) return true;
        }
        return false;
    }
}