import java.util.*;

/**
 * Builds a road if an opponent is close to stealing longest road and fires when another player
 * is within 1 road of the agent's longest road count
 */
public class DefendLongestRoadRule implements Rule {

    @Override
    public double evaluate(Player player, Board board, Resources resources) {
        if (!canAffordRoad(player)) return 0.0;

        int myLongest = calculateLongestRoad(player, board);

        // Check if any opponent comes close to longest road
        for (Player other : getAllPlayers(board)) {
            if (other.equals(player)) continue;

            int theirLongest = calculateLongestRoad(other, board);
            if (theirLongest >= myLongest - 1 && myLongest >= 5) {
                return 8.0; // High priority constraint
            }
        }

        return 0.0;
    }

    @Override
    public Command createCommand(Player player, Board board, Resources resources) {
        if (!canAffordRoad(player)) return null;

        Edge edge = findValidRoadLocation(player, board);
        if (edge != null) {
            return new BuildRoad(
                    edge.getIntersection1().getNodeID(),
                    edge.getIntersection2().getNodeID(),
                    board, resources, player
            );
        }

        return null;
    }

    @Override
    public String getName() { return "DefendLongestRoad"; }

    private boolean canAffordRoad(Player p) {
        return p.getResourceCount(Resource.BRICK) >= 1
                && p.getResourceCount(Resource.LUMBER) >= 1;
    }

    /**
     * Counts total roads owned
     */
    private int calculateLongestRoad(Player p, Board board) {
        int count = 0;
        for (Edge e : board.getEdges()) {
            if (p.equals(e.getOwner())) count++;
        }
        return count;
    }

    /**
     * Gets all players in the game
     */
    private List<Player> getAllPlayers(Board board) {
        Set<Player> players = new HashSet<>();
        for (Intersection i : board.getIntersections()) {
            if (i.getOwner() != null) {
                players.add(i.getOwner());
            }
        }
        return new ArrayList<>(players);
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