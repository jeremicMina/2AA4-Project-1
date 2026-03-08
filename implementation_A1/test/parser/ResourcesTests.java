import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

    //=========================
    //ResourcesTests.java
    // Tests for the Resources class and 'bank' management
    // reminder: resources are finit and held by bank.
    // They are earned by player and then returned to the bank when spent by the player.
    // expectations from the bank:
    //  - Give resources to a player if it has enough, refuse otherwise
    //  - Let a player spend if the player has enough resources in hand, refuse otherwise
    //=========================
    // =================================================================
    // Partitions for give/spend amounts:
    //   - P1: amount less or equal to zero -> invalid, return false
    //   - P2: amount valid, bank/player has less than needed -> return false
    //   - P3: amount valid, bank/player has exacrly enough -> boundary test, true
    //   - P4: amount valid, bank/player has more than needed -> true
    // =================================================================

public class ResourcesTests {

    private static final int TIMEOUT = 2000;
    private Resources bank;
    private Player player;

    /*
     * use @Before to set up objects before each test.
     * To avoid passing the same object to next method and carrying over a possible failure
     */
    @Before
    public void setUp() {
        // Default collection is 19 of every resource
        bank = Resources.createDefaultCollection();
        // Fresh player with no resources
        player = new Player(Color.WHITE, 21);
    }

    // =================================================================
    // tests for giveResources()
    // combine P3 + P4
    // combine
    // =================================================================

    /**
     * TEST Partition P4 and P3(success):
     * P4 : give 1 WOOL to player, bank has plenty -> return true, player WOOL inc by 1
     * P3 : boundary of bank is 19 ORE, if we ask for exactly 19 -> should be successfull
     */
    @Test(timeout = TIMEOUT)
    public void test5_giveResources_valid() {
        //P4
        boolean result = bank.giveResources(1, player, Resource.WOOL);
        assertTrue("giveResources should return true when bank has lots of WOOL", result);
        assertEquals("Player should now have 1 WOOL after receiving it from the bank", 1, player.getResourceCount(Resource.WOOL));

        //P3
        //rest player to avoid affects of prvevious test
        player2 = new Player(Color.WHITE, 21);
        boolean result2 = bank.giveResources(19, player2, Resource.ORE);
        assertTrue("Giving exactly 19 ORE which is the full bank stock should succeed", result2);
        assertEquals("Player should have all 19 ORE after draining our bank", 19, player2.getResourceCount(Resource.ORE));
    }

    /**
     * TEST Partition P2 and P1(bank doesn't have enough or number is invalid):
     * P2 : the default bank has 19 of each, asking for 20 -> fail
     * P1 : negative amount or zero is invalid -> return false
     */
    @Test(timeout = TIMEOUT)
    public void test6_giveResources_invalid() {
        // P2: Try to take 20, bank only has 19
        boolean result = bank.giveResources(20, player, Resource.BRICK);
        assertFalse("giveResources should return false when bank has only 19 BRICK", result);
        assertEquals("Player's BRICK count should still be 0", 0, player.getResourceCount(Resource.BRICK));

        //P1: giving 0 resources makes no sense
        assertFalse("GiveResources(0) should return false becasue of the boundary", bank.giveResources(0, player, Resource.LUMBER));
        //P1: negative amount is invalid
        assertFalse("giveResources(-1) should return false", bank.giveResources(-1, player, Resource.ORE));
    }

    // =================================================================
    // spendResources() TESTS
    //combine P4 + P3 + P2 + P1 to ensure it behaves correctly across all our partitions
    // =================================================================

    /**
     * TEST all Partition
     * P4 : player has enough and spends some -> return true and dec count of resource for player
     * P3 : (boundary test) spend exactly what the player has -> return true and count goes to 1
     * P2 : player doesn't have enough to spend -> return false
     * P1 : (boundary test) spending 0 is an invalid boundary -> return false
     */
    @Test(timeout = TIMEOUT)
    public void test7_spendResources() {
        // P4: Give player 3 GRAIN first, then spend 2 -> return true, player left with 1
        bank.giveResources(3, player, Resource.GRAIN);
        boolean result = bank.spendResources(2, player, Resource.GRAIN);
        assertTrue("spendResources should return true when player spends 2 GRAINS out of 3", result);
        assertEquals("Player should have 1 GRAIN left after spending 2 of 3", 1, player.getResourceCount(Resource.GRAIN));

        // P2: player starts with 0 WOOL and tries to spend 1 -> should fail
        boolean result2 = bank.spendResources(1, player, Resource.WOOL);
        assertFalse("spendResources should return false when player has 0 WOOL", result2);

        // P1: spending 0 is at the boundary of invalid -> should fail
        bank.giveResources(5, player, Resource.LUMBER);
        assertFalse("spendResources(0) should return false even if player has resources cause 0 is a boundary", bank.spendResources(0, player, Resource.LUMBER));

        // P3: give player 3 BRICK and spend 3 to test boundary -> success, player ends up with 0 resources
        bank.giveResources(3, player, Resource.BRICK);
        boolean result3 = bank.spendResources(3, player, Resource.BRICK);
        assertTrue("Spending exactly 3 BRICK when player has 3 should succeed", result3);
        assertEquals("Player should have 0 BRICK after spending all", 0, player.getResourceCount(Resource.BRICK));
    }

    // =================================================================
    // canProvideAll() TESTS
    // given a Map of demnad, method is used before distributing in ResourceProduction to avoid partial distribution
    // =================================================================

    /**
     * TEST: returns an answer based on a given map of demand
     * bank can satisfy normal demand (5 demand < 19 total)
     * bank cannot satisfy demand that exceeds stock for one resource
     * demand of 0 for everything should always return true for boundary testing
     * cannot satisfy after bank is partially depleted
     */
    @Test(timeout = TIMEOUT)
    public void test8_canProvideAll() {
        // ask for 5 rsource when stock is 19 -> true here
        Map<Resource, Integer> demand = new EnumMap<>(Resource.class);
        for (Resource r : Resource.values()) { demand.put(r, 5); }
        assertTrue("canProvideAll should return true when demand is within bank stock", bank.canProvideAll(demand));

        //ask for 20 ORE when the bank only has 19 -> return false
        for (Resource r : Resource.values()) { demand.put(r, 0); } //put all demands to zero
        demand.put(Resource.ORE, 20); // bank stock is 19
        assertFalse("canProvideAll should return false when ORE demand exceeds bank stock", bank.canProvideAll(demand));

        //0 demand is a valid edge case -> return true
        for (Resource r : Resource.values()) { demand.put(r, 0); }
        assertTrue("canProvideAll with 0 demand should return true", bank.canProvideAll(demand));

        // after bank is partially depleted, check remaining stock
        // drain 18 of WOOL, leaving 1. Demand for 2 WOOL → fails
        bank.giveResources(18, player, Resource.WOOL);
        for (Resource r : Resource.values()) demand.put(r, 0);
        demand.put(Resource.WOOL, 2); //change demand of wool to 2
        assertFalse("canProvideAll must return false when WOOL demand exceeds remaining bank stock", bank.canProvideAll(demand));
    }
}
