import java.util.*;

/**
 * The game class implements one instance and encapsulates one simulation of the game through keeping track
 * of the currentRound, maxRound, the 4 players identified through colors and is implemented by the use of
 * helper method like start and end the game, switching turns between the players and validating the moves of
 * the players to see if they can afford their desired moves as well as keeping track of the points and setting
 * the initial state of the game by distributing the right number of resources to each player.
 */

public class Game {
    private int currentRound;
    private int maxRound;

    private final Board board;
    private final Resources resources;
    private final ResourceProduction production;

    private Player orange;
    private Player white;
    private Player red;
    private Player blue;

    private final Random randomizer;

    /**
     * The game instance constructor to initiate one game simulation
     * @param board the board where the game will be hosted
     * @param resources the resources that will be initialized in the game and dealt with during the game
     * @param production the production of resources through the special class of resourceProduction
     * @param maxRound the maximum number of round that will be stated to 25 in the simulation
     * @param randomizer the randomizer to use throughout the game to generate random moves
     */
    public Game(Board board, Resources resources, ResourceProduction production, int maxRound, Random randomizer) {
        this.board = board;
        this.resources = resources;
        this.production = production;
        this.maxRound = maxRound;
        this.randomizer = randomizer;

        orange = new Player(Color.ORANGE, 19);
        white  = new Player(Color.WHITE, 20);
        red    = new Player(Color.RED, 21);
        blue   = new Player(Color.BLUE, 22);

        currentRound = 0;
        setupInitialPlacements();
    }

    // Start game method to keep playing until the maxround is hit
    public void start() {
        while (currentRound < maxRound && !checkWinner()) {
            currentRound++;
            playRound();
            printVictoryPointsSummary();
        }
        endGame();
    }

    // The simulation of round per each player
    public void playRound() {
        for (Player p : List.of(orange, white, red, blue)) {
            playTurn(p);
        }
    }

    // The simulation of each turn of each player passed as paramter to the method
    private void playTurn(Player currentPlayer) {
        boolean produced = production.produce(orange, white, red, blue);

        System.out.println("=== Round " + currentRound + ", " + currentPlayer.getColor() + "'s turn ===");
        if (produced) System.out.println("Resources produced for eligible settlements/cities.");
        else System.out.println("No production this turn.");

        if (currentPlayer.totalResourceCards() > 7) {
            boolean spent = encourageSpending(currentPlayer);
            if (!spent) System.out.println(currentPlayer.getColor() + " has >7 cards but could not build anything.");
            pause(); // <- pause for visibility
            return;
        }

        List<RunnableAction> actions = computeLegalActions(currentPlayer);
        if (!actions.isEmpty()) {
            RunnableAction chosen = actions.get(randomizer.nextInt(actions.size()));
            chosen.run();
        } else {
            System.out.println(currentPlayer.getColor() + " has no legal build action available.");
        }

        pause(); // pause after each turn
    }

    // Pause method to run the simulation in slow mode so we can watch it live instead of running the full code all at once
    private void pause() {
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
    }

    // R1.8: when the player has 7 cards, we will use this method to encourage him to spend
    private boolean encourageSpending(Player p) {
        List<RunnableAction> actions = computeLegalActions(p);
        if (actions.isEmpty()) return false;
        RunnableAction chosen = actions.get(randomizer.nextInt(actions.size()));
        chosen.run();
        System.out.println("Player was encouraged to spend (>7 cards) and build something.");
        return true;
    }

    // ComputeLegalActions is the method that is responsible to run the runnable action per player passed as parameter
    // such as building city, settlement or road
    private List<RunnableAction> computeLegalActions(Player p) {
        List<RunnableAction> actions = new ArrayList<>();

        if (canAffordCity(p)) {
            for (Intersection i : board.getIntersections()) {
                final Intersection target = i;
                if (p.equals(target.getOwner()) && !target.isCity()) {
                    actions.add(new RunnableAction(() -> {
                        if (payCityCost(p) && board.buildCity(p, target)) {
                            log(p, "built CITY at node " + target.getNodeID());
                        }
                    }));
                }
            }
        }

        if (canAffordSettlement(p)) {
            for (Intersection i : board.getIntersections()) {
                final Intersection target = i;
                if (target.getOwner() == null && hasAdjacentPlayerRoad(p, target) && neighborsFree(target)) {
                    actions.add(new RunnableAction(() -> {
                        if (paySettlementCost(p) && board.buildSettlement(p, target)) {
                            log(p, "built SETTLEMENT at node " + target.getNodeID());
                        }
                    }));
                }
            }
        }

        if (canAffordRoad(p)) {
            for (Edge e : board.getEdges()) {
                final Edge targetEdge = e;
                if (targetEdge.getOwner() == null) {
                    actions.add(new RunnableAction(() -> {
                        if (payRoadCost(p) && board.buildRoad(p, targetEdge)) {
                            log(p, "built ROAD on edge " + targetEdge.getEdgeID());
                        }
                    }));
                }
            }
        }

        return actions;
    }

    // Checker method to make sure the player has the adjacent road to help us locate the player and its intent
    private boolean hasAdjacentPlayerRoad(Player p, Intersection target) {
        for (Edge e : target.getEdges()) {
            if (p.equals(e.getOwner())) return true;
        }
        return false;
    }

    // Checker method to make sure his surroundings are free
    private boolean neighborsFree(Intersection target) {
        for (Intersection n : target.getAdjacentIntersections()) {
            if (n.getOwner() != null) return false;
        }
        return true;
    }

    // Checker method to check if the player passed as param can afford the road
    private boolean canAffordRoad(Player p) {
        return p.getResourceCount(Resource.BRICK) >= 1 && p.getResourceCount(Resource.LUMBER) >= 1;
    }

    // Checker method to check if the player passed as param can afford the settlement
    private boolean canAffordSettlement(Player p) {
        return p.getResourceCount(Resource.BRICK) >= 1
                && p.getResourceCount(Resource.LUMBER) >= 1
                && p.getResourceCount(Resource.WOOL) >= 1
                && p.getResourceCount(Resource.GRAIN) >= 1;
    }

    // Checker method to check if the player passed as param can afford the city
    private boolean canAffordCity(Player p) {
        return p.getResourceCount(Resource.ORE) >= 3 && p.getResourceCount(Resource.GRAIN) >= 2;
    }

    // Checker method to have the palyer passed as param pay the resources required to build a road
    private boolean payRoadCost(Player p) {
        return resources.spendResources(1, p, Resource.BRICK)
                && resources.spendResources(1, p, Resource.LUMBER);
    }

    // Checker method to have the palyer passed as param pay the resources required to build a settlement
    private boolean paySettlementCost(Player p) {
        return resources.spendResources(1, p, Resource.BRICK)
                && resources.spendResources(1, p, Resource.LUMBER)
                && resources.spendResources(1, p, Resource.WOOL)
                && resources.spendResources(1, p, Resource.GRAIN);
    }

    // Checker method to have the palyer passed as param pay the resources required to build a city
    private boolean payCityCost(Player p) {
        return resources.spendResources(3, p, Resource.ORE)
                && resources.spendResources(2, p, Resource.GRAIN);
    }

    // Checker method to check the winner if any player reached the 10 vpoints
    public boolean checkWinner() {
        return getVictoryPoints(orange) >= 10
                || getVictoryPoints(white) >= 10
                || getVictoryPoints(red) >= 10
                || getVictoryPoints(blue) >= 10;
    }

    // Getter method to get the vpoints of the player passed in param
    private int getVictoryPoints(Player p) {
        int vp = 0;
        for (Intersection i : board.getIntersections()) {
            if (p.equals(i.getOwner())) {
                vp += i.isCity() ? 2 : 1;
            }
        }
        return vp;
    }

    // Method used to end the game and to announce the winner and his vpoints
    public void endGame() {
        Player winner = null;
        int best = -1;
        for (Player p : List.of(orange, white, red, blue)) {
            int vp = getVictoryPoints(p);
            if (vp > best) {
                best = vp;
                winner = p;
            }
        }
        System.out.println("=== GAME ENDED ===");
        System.out.println("Winner: " + winner.getColor() + " with " + best + " VP.");
    }

    // Setter method to initialize the placements at the start of the game
    private void setupInitialPlacements() {
        List<Player> order = List.of(orange, white, red, blue);
        for (Player p : order) placeInitialSettlementAndRoad(p);
        for (Player p : order) {
            placeInitialSettlementAndRoad(p);
            grantStartingResourcesForSecondSettlement(p);
        }
    }
    // Setter method to place the settlements and roads for the player passed as param
    private void placeInitialSettlementAndRoad(Player p) {
        List<Intersection> candidates = new ArrayList<>();
        for (Intersection i : board.getIntersections()) {
            if (i.getOwner() != null) continue;
            if (neighborsFree(i)) candidates.add(i);
        }
        if (candidates.isEmpty()) return;

        Intersection chosen = candidates.get(randomizer.nextInt(candidates.size()));
        if (board.buildSettlement(p, chosen, true)) {
            log(p, "initial placement: SETTLEMENT at node " + chosen.getNodeID());
        }

        List<Edge> edgeCandidates = new ArrayList<>();
        for (Edge e : chosen.getEdges()) {
            if (e.getOwner() == null) edgeCandidates.add(e);
        }
        if (!edgeCandidates.isEmpty()) {
            Edge roadEdge = edgeCandidates.get(randomizer.nextInt(edgeCandidates.size()));
            roadEdge.setOwner(p);
            p.recordRoadBuilt(roadEdge.getEdgeID());
            log(p, "initial placement: ROAD on edge " + roadEdge.getEdgeID());
        }
    }

    // Method used to grant the resources for the second settlement for the player passed as param
    private void grantStartingResourcesForSecondSettlement(Player p) {
        List<Intersection> ownedSettlements = new ArrayList<>();
        for (Intersection i : board.getIntersections()) {
            if (p.equals(i.getOwner()) && !i.isCity()) ownedSettlements.add(i);
        }
        if (ownedSettlements.isEmpty()) return;

        Intersection chosen = ownedSettlements.get(randomizer.nextInt(ownedSettlements.size()));
        Map<Resource, Integer> demand = new EnumMap<>(Resource.class);
        for (Resource r : Resource.values()) demand.put(r, 0);
        for (Tile t : chosen.getTiles()) {
            Resource produced = Tile.getResource(t.getTerrain());
            if (produced != null) demand.put(produced, demand.get(produced) + 1);
        }

        if (!resources.canProvideAll(demand)) {
            log(p, "starting resources skipped (bank shortage).");
            return;
        }

        for (Resource r : Resource.values()) {
            int amt = demand.get(r);
            if (amt > 0) resources.giveResources(amt, p, r);
        }
        log(p, "received starting resources for initial placement.");
    }

    // Log method used to print for each round the state of the round, the player and his action
    private void log(Player p, String action) {
        System.out.println(currentRound + " / " + p.getColor() + ": " + action);
    }

    // Method used to print the vpoints summary for each round
    private void printVictoryPointsSummary() {
        System.out.println(currentRound + " / SYSTEM: VP summary -> "
                + "ORANGE=" + getVictoryPoints(orange) + ", "
                + "WHITE=" + getVictoryPoints(white) + ", "
                + "RED=" + getVictoryPoints(red) + ", "
                + "BLUE=" + getVictoryPoints(blue));
    }

    // The runnable action method instantiated to be used in the boardConfig class
    private static final class RunnableAction {
        private final Runnable r;
        RunnableAction(Runnable r) { this.r = r; }
        void run() { r.run(); }
    }
}
