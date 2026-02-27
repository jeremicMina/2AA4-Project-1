import java.util.*;

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
    public boolean produce(Player currentPlayer, List<Player> players) {
        //Rolling the dice object passed along
        int rollSum = dice.roll();

        // Send the game flow to the handleRobber method if rolled a 7
        if (rollSum == 7) {
            handleRobber(currentPlayer, players);
            return false;
        }

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
            // Adding the robber factor to skip over it so no production happens when robber is implemented
            if(t == board.getRobberTile()){
                continue; // This will allow the robber to block production
            }
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

    /**
     * handleRobber is a method that gets implemented when rolling a 7 and takes as param
     * the list of the players, then deducts half of the cards out of the players that hold
     * 7 or more cards, as well as moves the robber to a random tile and choses the victim player
     * if applicable and steals a card from him.
     *
     * @param currentPlayer
     * @param players       the list of the players participating in the game
     */
    private void handleRobber(Player currentPlayer, List<Player> players) {

        // 1. Discard half if >7 cards
        for (Player p : players) {
            int total = p.totalResourceCards();
            if (total > 7) {
                int toDiscard = total / 2; // floor automatically
                discardRandomCards(p, toDiscard);
            }
        }

        // 2. Move robber randomly
        List<Tile> allTiles = board.getTiles();
        Tile newTile = allTiles.get(new java.util.Random().nextInt(allTiles.size()));
        board.setRobberTile(newTile);

        // 3. Determine eligible victims
        List<Player> eligible = new ArrayList<>();

        for (Intersection i : newTile.getIntersections()) {
            Player owner = i.getOwner();
            if (owner != null && !eligible.contains(owner)) {
                eligible.add(owner);
            }
        }

        if (eligible.isEmpty()) return;

        // 4. Random victim
        Player victim = eligible.get(new java.util.Random().nextInt(eligible.size()));

        // 5. Random steal
        stealRandomCard(players.get(0), victim);
    }

    /**
     * discardRandomCard is a method used to discard half rounded down the number of cards randomly
     * @param p the player that will be deducted cards
     * @param amount the amount of cards that should be discarded from the player p
     */
    private void discardRandomCards(Player p, int amount) {

        // Creating a pool of resources that the player has
        List<Resource> pool = new ArrayList<>();

        // Adding the resources existant to the pool of resources for that player
        for (Resource r : Resource.values()) {
            for (int i = 0; i < p.getResourceCount(r); i++) {
                pool.add(r);
            }
        }
        // Shuffling the resources so they are all mixed instead of having them listed one after the other
        java.util.Collections.shuffle(pool);

        // Spending the resources set by the amount passed as param
        for (int i = 0; i < amount && i < pool.size(); i++) {
            resources.spendResources(1, p, pool.get(i));
        }
    }

    /**
     * stealRandomCard is a method that picks a random card and takes it away from the victim
     * player and gives it to the thief.
     * @param thief Player that will get the benefit of the random card
     * @param victim Player that will have to give up one of his random cards
     */
    private void stealRandomCard(Player thief, Player victim) {

        //Creating a list of resources from the victim cards
        List<Resource> victimCards = new ArrayList<>();

        // Adding the resources to the victimCards
        for (Resource r : Resource.values()) {
            for (int i = 0; i < victim.getResourceCount(r); i++) {
                victimCards.add(r);
            }
        }

        // Checking that the list has resources for the victim player
        if (victimCards.isEmpty()) return;

        // Shuffling the victim cards so they all well mixed before picking one of them
        java.util.Collections.shuffle(victimCards);

        // Picking the first card as the random card that will be stolen
        Resource stolen = victimCards.get(0);

        // Disptaching the card from the victim to the thief
        resources.spendResources(1, victim, stolen);
        resources.giveResources(1, thief, stolen);
    }
}
