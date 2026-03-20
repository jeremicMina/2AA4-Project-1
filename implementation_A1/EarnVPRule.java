/**
 * Earning a victory point is worth 1.0.
 */
public class EarnVPRule implements Rule {

    @Override
    public double evaluate(Player player, Board board, Resources resources) {
        if (canAffordSettlement(player) && findValidSettlementLocation(player, board) != -1) {
            return 1.0;
        }
        if (canAffordCity(player) && findSettlementToUpgrade(player, board) != -1) {
            return 1.0;
        }
        return 0.0;
    }

    @Override
    public Command createCommand(Player player, Board board, Resources resources) {
        // Try settlement first
        if (canAffordSettlement(player)) {
            int nodeId = findValidSettlementLocation(player, board);
            if (nodeId != -1) {
                return new BuildSettlement(nodeId, board, resources, player);
            }
        }

        // Try city upgrade if not settlement
        if (canAffordCity(player)) {
            int nodeId = findSettlementToUpgrade(player, board);
            if (nodeId != -1) {
                return new BuildCity(nodeId, board, resources, player);
            }
        }

        return null;
    }

    @Override
    public String getName() { return "EarnVP"; }

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