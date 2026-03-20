import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Roll represents the rolling action of dice and also distribution of resources resulted from rolling
 *
 * ommand pattern implementation:
 *      - execute(): rolls the dice through ResourceProduction.produce(), snapshots what was given out
 *      - undo(): takes back what was distributed using the snapshot that was stored of roll
 *
 * most complex implementation of undo() because produce() distributes resources to multiple players at once.
 * we save a snapshot during execute() so undo() knows what to undo
 *
 * the robber (roll of 7) is a special case and produce() handles the robber internally and returns false
 * so no snapshot is stored and undo() has nothing to reverse for the robber roll of 7
 * robber effects (discarded cards, stolen card) are intentionally and not undoable
 * since they involve randomness and can't be cleanly reversed
 */

public class Roll implements Command {

    //context injected so execute() and undo() can act on game state
    private final ResourceProduction production;
    private final Resources resources;
    private final Player currentPlayer;
    private final List<Player> allPlayers;

    /**
     * snapshot of resources distributed during execute()
     * the outer map: player -> (resource -> amount given)
     * only populated when produce() returns true and resources were actually given out
     * stays null if no production happened
     */
    private Map<Player, Map<Resource, Integer>> snapshot;

    /**
     * no-arg constructor to be used by CommandParser
     * creates a Roll marker that the game loop intercepts and executes directly
     * execute() and undo() do nothing when context is null
     */
    public Roll() {
        this.production = null;
        this.resources = null;
        this.currentPlayer = null;
        this.allPlayers = null;
    }

    /**
     * constructor for a Roll command
     *
     * @param production    the ResourceProduction instance handeling dice roll + distribution
     * @param resources     the resource bank used to take back resources on undo
     * @param currentPlayer the player whose turn it is and passed to produce()
     * @param allPlayers    all four players
     */
    public Roll(ResourceProduction production, Resources resources,
                Player currentPlayer, List<Player> allPlayers) {
        this.production = production;
        this.resources = resources;
        this.currentPlayer = currentPlayer;
        this.allPlayers = allPlayers;
    }

    /**
     * executes the roll action:
     * 1. takes a before-snapshot of every player's resources
     * 2. calls production.produce() to roll the dice and distribute resources
     * 3. takes an after-snapshot of every player's resources
     * 4. computes the diff from both snapshots to know exactly what was given to whom
     * 5. stores the diff as the actual snapshot for undo()
     *
     * if produce() returns false (robber or no production), the diff will be 0 and snapshot stays null
     */
    @Override
    public void execute() {

        // if no context injected Roll was created by parser as a signal only
        if (production == null) return;

        //take a before-snapshot of every player's resource counts
        Map<Player, Map<Resource, Integer>> before = takeSnapshot();

        //roll the dice and distribute using produce()
        production.produce(currentPlayer, allPlayers);

        //take an after-snapshot
        Map<Player, Map<Resource, Integer>> after = takeSnapshot();

        //compute the diff
        Map<Player, Map<Resource, Integer>> diff = computeDiff(before, after);

        //only store snapshot if something was distributed
        //if diff is all 0; leave snapshot null
        if (anyPositive(diff)) {
            snapshot = diff;
        }
    }

    /**
     * reverses the roll action:
     * 1. checks snapshot and if null; nothing to reverse
     * 2. for each player, takes back exactly the resources they received
     *    by calling resources.spendResources() which moves them back to bank
     * 3. resets snapshot to null so this command can't be undone again
     *
     * Note: the robber's discard and steal effects are not reversed
     */
    @Override
    public void undo() {
        //if nothing distributed; nothing to undo
        if (snapshot == null) return;

        // take back exactly what was given during execute()
        for (Map.Entry<Player, Map<Resource, Integer>> playerEntry : snapshot.entrySet()) {

            Player p = playerEntry.getKey();
            Map<Resource, Integer> received = playerEntry.getValue();

            for (Map.Entry<Resource, Integer> resourceEntry : received.entrySet()) {
                Resource r = resourceEntry.getKey();
                int amount = resourceEntry.getValue();

                if (amount > 0) {
                    //spendResources moves cards from player back to bank
                    resources.spendResources(amount, p, r);
                }
            }
        }

        //clear the snapshot so this command can't be undone twice
        snapshot = null;
    }

    /**
     * Takes a deep copy snapshot of every player's current resource counts
     * important cause if we stored a reference to the live map, the before-snapshot would change as produce() runs
     *
     * @return map of player -> (resource -> count) as of rn
     */
    private Map<Player, Map<Resource, Integer>> takeSnapshot() {

        // HashMap used because Player is not an enum type so EnumMap cant be used as the outer map key
        Map<Player, Map<Resource, Integer>> snap = new java.util.HashMap<>();

        for (Player p : allPlayers) {
            Map<Resource, Integer> counts = new EnumMap<>(Resource.class);
            for (Resource r : Resource.values()) {
                counts.put(r, p.getResourceCount(r));
            }
            snap.put(p, counts);
        }
        return snap;
    }

    /**
     * computes the difference between two snapshots
     * diff = after - before
     * positive values mean player received that resource during produce()
     * Zero or negative values mean no change or spending, ignored here
     *
     * @param before snapshot taken before produce()
     * @param after  snapshot taken after produce()
     * @return diff map showing exactly what each player gained
     */
    private Map<Player, Map<Resource, Integer>> computeDiff(
            Map<Player, Map<Resource, Integer>> before,
            Map<Player, Map<Resource, Integer>> after) {

        Map<Player, Map<Resource, Integer>> diff = new java.util.HashMap<>();

        for (Player p : allPlayers) {
            Map<Resource, Integer> diffForPlayer = new EnumMap<>(Resource.class);
            for (Resource r : Resource.values()) {
                int gained = after.get(p).get(r) - before.get(p).get(r);
                // only record positive diff
                diffForPlayer.put(r, Math.max(0, gained));
            }
            diff.put(p, diffForPlayer);
        }
        return diff;
    }

    /**
     * checks whether diff map contains any positive value to decide whether store snapshot or not
     * if everything is zero, nothing to undo
     *
     * @param diff the computed diff map
     * @return true if at least one player received at least one resource
     */
    private boolean anyPositive(Map<Player, Map<Resource, Integer>>diff) {
        for (Map<Resource, Integer> playerDiff : diff.values()) {
            for (int amount : playerDiff.values()) {
                if (amount > 0) return true;
            }
        }
        return false;
    }

    @Override
    public String name() {
        return "ROLL";
    }

    @Override
    public String toString() {
        return "ROLL";
    }
}
