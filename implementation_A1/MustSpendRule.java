/**
 * Forces player to spend cards if they have more than 7, avoids losing cards to the robber.
 */
public class MustSpendRule implements Rule {

    @Override
    public double evaluate(Player player, Board board, Resources resources) {
        if (player.totalResourceCards() > 7) {
            return 10.0; // Highest priority constraint
        }
        return 0.0;
    }

    @Override
    public Command createCommand(Player player, Board board, Resources resources) {
        if (player.totalResourceCards() <= 7) return null;

        // Try to build the cheapest thing to reduce card count
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

        if (canAffordSettlement(player)) {
            int nodeId = findValidSettlementLocation(player, board);
            if (nodeId != -1) {
                return new BuildSettlement(nodeId, board, resources, player);
            }
        }

        if (canAffordCity(player)) {
            int nodeId = findSettlementToUpgrade(player, board);
            if (nodeId != -1) {
                return new BuildCity(nodeId, board, resources, player);
            }
        }

        return null;
    }

    @Override
    public String getName() { return "MustSpend"; }

    // Resource checking
    private boolean canAffordRoad(Player p) {
        return p.getResourceCount(Resource.BRICK) >= 1
                && p.getResourceCount(Resource.LUMBER) >= 1;
    }

    private boolean canAffordSettlement(Player p) {
        return p.getResourceCount(Resource.BRICK) >= 1
                && p.getResourceCount(Resource.LUMBER) >= 1
                && p.getResourceCount(Resource.WOOL) >= 1
                && p.getResourceCount(Resource.GRAIN) >= 1;
    }

    private boolean canAffordCity(Player p) {
        return p.getResourceCount(Resource.ORE) >= 3
                && p.getResourceCount(Resource.GRAIN) >= 2;
    }

    // Location finding
    private Edge findValidRoadLocation(Player p, Board board) {
        for (Edge e : board.getEdges()) {
            if (e.getOwner() == null && isConnectedToPlayer(p, e)) {
                return e;
            }
        }
        return null;
    }

    private int findValidSettlementLocation(Player p, Board board) {
        for (Intersection i : board.getIntersections()) {
            if (i.getOwner() == null && neighborsFree(i) && hasAdjacentRoad(p, i)) {
                return i.getNodeID();
            }
        }
        return -1;
    }

    private int findSettlementToUpgrade(Player p, Board board) {
        for (Intersection i : board.getIntersections()) {
            if (p.equals(i.getOwner()) && !i.isCity()) {
                return i.getNodeID();
            }
        }
        return -1;
    }

    // Validation helpers
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

    private boolean neighborsFree(Intersection target) {
        // No settlements within 2 edges
        for (Intersection n : target.getAdjacentIntersections()) {
            if (n.getOwner() != null) return false;
        }
        return true;
    }

    private boolean hasAdjacentRoad(Player p, Intersection target) {
        // Settlement must be adjacent to players road
        for (Edge e : target.getEdges()) {
            if (p.equals(e.getOwner())) return true;
        }
        return false;
    }
}