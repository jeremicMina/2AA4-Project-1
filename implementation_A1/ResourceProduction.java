import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ResourceProduction is a class that takes the dice, board and resources to generate methods like produce
 * that implements implicitely the production of resources depending on the dice rolled and the actions
 * passed, and it relies on many factors such as the availability of the resources from the bank
 */
public class ResourceProduction {
    private Dice dice;
    private Resources resources;
    private Board board;

    /**
     * Constructor used to generate one instance of the resourceProduction object that will be used to
     * dispatch and produce resources.
     * @param dice the dice that will be used to roll to implement a number
     * @param resources the resources passed from the resources class
     * @param board the instance of the board where the game is hosted
     */
    public ResourceProduction(Dice dice, Resources resources, Board board) {
        this.dice = dice;
        this.resources = resources;
        this.board = board;
    }

    /**
     * @param players a list of players in the game (production affects all players)
     * @return true if resources were successfully produced and distributed; false otherwise
     */
    public boolean produce(List<Player> players) {
        //Rolling the dice object passed along
        int rollSum = dice.roll();

        // 1) Find producing tiles
        List<Tile> producingTiles = board.getTilesByToken(rollSum);
        if (producingTiles.isEmpty()) {
            return false;
        }

        // 2) Compute total demand (avoid partial distribution)
        EnumMap<Resource, Integer> totalDemand = new EnumMap<>(Resource.class);
        for (Resource r : Resource.values()) {
            totalDemand.put(r, 0);
        }

        // Track each player's individual demand to apply distribution after bank check
        Map<Player, EnumMap<Resource, Integer>> perPlayerDemand = new HashMap<>();
        for (Player p : players) {
            EnumMap<Resource, Integer> d = new EnumMap<>(Resource.class);
            for (Resource r : Resource.values()) d.put(r, 0);
            perPlayerDemand.put(p, d);
        }

        for (Tile t : producingTiles) {
            Resource produced = Tile.getResource(t.getTerrain());
            if (produced == null) continue; // desert or non-producing

            for (Intersection inter : t.getIntersections()) {
                Player owner = inter.getOwner();
                if (owner == null) continue;

                int amount = inter.isCity() ? 2 : 1;

                // accumulate demand
                perPlayerDemand.get(owner).put(produced, perPlayerDemand.get(owner).get(produced) + amount);
                totalDemand.put(produced, totalDemand.get(produced) + amount);
            }
        }

        // If nobody demands anything, return false
        boolean anyDemand = false;
        for (Resource r : Resource.values()) {
            if (totalDemand.get(r) > 0) {
                anyDemand = true;
                break;
            }
        }
        if (!anyDemand) return false;

        // 3) Check bank can provide everything
        if (!resources.canProvideAll(totalDemand)) {
            return false;
        }

        // 4) Distribute (each call deducts from bank)
        for (Map.Entry<Player, EnumMap<Resource, Integer>> e : perPlayerDemand.entrySet()) {
            Player p = e.getKey();
            EnumMap<Resource, Integer> d = e.getValue();
            for (Resource r : Resource.values()) {
                int amount = d.get(r);
                if (amount > 0) {
                    // should always succeed because we checked canProvideAll
                    resources.giveResources(amount, p, r);
                }
            }
        }

        return true;
    }

    // Overloading the produce method to accomodate for when the method is passed with the 4 players as params
    public boolean produce(Player orange, Player white, Player red, Player blue) {
        return produce(List.of(orange, white, red, blue));
    }
}
