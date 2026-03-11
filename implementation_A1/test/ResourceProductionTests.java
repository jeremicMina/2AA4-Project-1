import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

//=========================
// ResourceProductionTests.java
// Tests for ResourceProduction.produce()
// produce() rolls the dice, finds matching tiles, and gives resources to players who have buildings on those tiles
// The dice is a test stub and not an actual random dice for this test since we need to be able to predict the behaviour
//=========================

public class ResourceProductionTests {

    private static final int TIMEOUT = 2000;
    private Board board;
    private Resources bank;
    private Player p1, p2, p3, p4;

    /*
     * use @Before to set up objects before each test.
     * To avoid passing the same object to next method and carrying over a possible failure
     */
    @Before
    public void setUp() {
        board = new Board();
        bank  = Resources.createDefaultCollection();
        p1 = new Player(Color.ORANGE, 20);
        p2 = new Player(Color.WHITE, 21);
        p3 = new Player(Color.RED, 22);
        p4 = new Player(Color.BLUE, 23);
    }

    /**
     * HELPER: create a fake Dice that always returns a fixed number
     * do not throw the object away after
     */
    private Dice fixedDice(int fixedRoll) { return () -> fixedRoll; }

    /**
     * HELPER: find a free intersection on a specific tile
     * searches for a node that has no owner and no adjacent owners cause of distance rule
     * returns null if no such node exists
     */
    private Intersection findFreeIntersectionOn(Tile tile) {
        for (Intersection i : tile.getIntersections()) {
            if (i.getOwner() != null) {continue;}
            boolean neighboursFree = true;
            for (Intersection n : i.getAdjacentIntersections()) {
                if (n.getOwner() != null) { neighboursFree = false; break; }
            }
            if (neighboursFree) {return i;}
        }
        return null;
    }

    // =================================================================
    // GROUP 1: tests for produce() when nothing should be produced
    // =================================================================

    /**
     * TEST: produce() returns false when no production is happening
     *          partition P2 -> token 6 tiles exist but no player has placed anywhere,
     *          no demand is generated so produce() must also return false
     */
    @Test(timeout = TIMEOUT)
    public void test19_produce_noOutput() {
        // P1: roll 7, no tile has token 7 in default board (since 7 triggers robber and has not production) -> false
        ResourceProduction resourceP = new ResourceProduction(fixedDice(7), bank, board);
        assertFalse("produce should return false when no tile has the rolled token number 7", resourceP.produce(p1, List.of(p1, p2, p3, p4)));

        // P2: roll 6, token 6 tiles exist but nobody has placed a settlement yet -> false
            resourceP = new ResourceProduction(fixedDice(6), bank, board);
        assertFalse("produce should return false when matching tiles exist but no player occupies", resourceP.produce(p1, List.of(p1, p2, p3, p4)));
    }

    // =================================================================
    // GROUP 2: tests for produce() when production should happen
    //   P3: player owns a settlement on a matching tile -> true, player gets 1 resource
    //   P4: player owns a city on a matching tile -> true, gets 2 resources
    // =================================================================

    /**
     * TEST: produce() gives the correct amount of resources when conditions met
     */
    @Test(timeout = TIMEOUT)
    public void test20_produce_withOutput() {
        // find a token 6 tile to place settlements on
        List<Tile> token6Tiles = board.getTilesByToken(6);
        assertFalse("we need at least one tile with token 6 to exist", token6Tiles.isEmpty());

        Tile tile = token6Tiles.get(0);
        Resource expectedResource = Tile.getResource(tile.getTerrain());
        assertNotNull("the token-6 tile shouldn't be a desert", expectedResource);

        //find a intersection node that is free
        Intersection node = findFreeIntersectionOn(tile);
        assertNotNull("find a free intersection on the token-6 tile", node);

        // P3: place a settlement on the token6 tile, roll 6 -> get exactly 1 resource
        board.buildSettlement(p1, node, true); // it's an initial placement so no road needed
        int before = p1.getResourceCount(expectedResource);
        ResourceProduction resourceP = new ResourceProduction(fixedDice(6), bank, board);
        boolean produced = resourceP.produce(p1, List.of(p1, p2, p3, p4));

        assertTrue("produce should return true when a player has a settlement on a token6 tile", produced);
        assertEquals("Settlement should yield exactly 1 resource" + expectedResource, before + 1, p1.getResourceCount(expectedResource));

        // P4: upgrade that same node to a city by rolling 6 again -> get exactly 2 resources
        board.buildCity(p1, node); // upgrade settlement to city
        int beforeCity = p1.getResourceCount(expectedResource);
        resourceP = new ResourceProduction(fixedDice(6), bank, board);
        resourceP.produce(p1, List.of(p1, p2, p3, p4));

        assertEquals("City must yield exactly 2 " + expectedResource + " (it's double a settlement)", beforeCity + 2, p1.getResourceCount(expectedResource));
    }
}
