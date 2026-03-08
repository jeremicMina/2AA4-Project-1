import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

    //=========================
    //PlayerTests.java
    //Test for Player class - resource management and recordkeeping
    //=========================

public class PlayerTests {

    private static final int TIMEOUT = 2000;
    private Player player;

    /*
     * use @Before to set up objects before each test.
     * To avoid passing the same object to next method and carrying over a possible failure
     */
    @Before
    public void setUp() {
        // Create a new 'ORANGE' player aged 20 before every test
        player = new Player(Color.ORANGE, 20);
    }

    // =================================================================
    // GROUP 1: tests for initial state
    // Partitions: what must be true in initial state when player is created
    // =================================================================

    /**
     * TEST: new player at initial state starts
     * PURPOSE:
     * - loop over every Resource enum value and verify initial resources count = 0 is true
     * - ensure player has 0 total resource cards
     * - get the colour of the player and make sure it is set accurately
     */
    @Test(timeout = TIMEOUT)
    public void test1_initialState() {
        for (Resource r : Resource.values()) {
            assertEquals("Player should start with 0 of " + r, 0, player.getResourceCount(r));
        }
        assertEquals("Total resource cards should be 0 for a brand new player", 0, player.totalResourceCards());
        assertEquals("getColor should return ORANGE since that is what we passed in", Color.ORANGE, player.getColor());
    }

    // =================================================================
    // GROUP 2: tests for addResource
    // Partitions:
    // A) Add a positive amount -> should increase the count
    // B) Add to multiple types -> total should sum up correctly
    // C) Add exactly 1         -> Boundary testing
    // =================================================================

    /**
     * TEST: addResource increases count by the correct amoun
     *PURPOSE: partition A, B, and C
     */
    @Test(timeout = TIMEOUT)
    public void test2_addResource() {
        // Partition A: give the player 3 WOOL
        player.addResource(Resource.WOOL, 3);
        assertEquals("Player should have 3 WOOL after addResource(WOOL, 3)", 3, player.getResourceCount(Resource.WOOL));

        // Partition B: add multiple different types and verify the total sum
        // reset player so we know exactly what total should be and not to pass previous tests failed results
        player = new Player(Color.ORANGE, 20);
        player.addResource(Resource.WOOL,2);
        player.addResource(Resource.BRICK, 3);
        player.addResource(Resource.LUMBER, 1);
        // 2 + 3 + 1 = 6
        assertEquals("Total should be 6 after adding WOOL=2, BRICK=3, LUMBER=1", 6, player.totalResourceCards());

        //Partition C: boundary testing exactly at 1
        player = new Player(Color.ORANGE, 20);
        player.addResource(Resource.ORE, 1);
        assertEquals("BOUNDARY: Player should have exactly 1 ORE", 1, player.getResourceCount(Resource.ORE));
    }

    // =================================================================
    // GROUP 3: tests for removeResource
    // Partitions:
    // A) Remove some from a pile that has enough  -> count decreases
    // B) Remove all of a resource                 -> count goes to 0
    // C) For boundary testing add 1 then remove 1 -> back to 0
    // =================================================================

    /**
     * TEST: removeResource decreases
     * PURPOSE: partition A, B, C
     */
    @Test(timeout = TIMEOUT)
    public void test3_removeResource_normalRemove_decreasesCount() {
        // Partition A: add 5 GRAIN, remove 2, should leave exactly 3
        player.addResource(Resource.GRAIN, 5);
        player.removeResource(Resource.GRAIN, 2);
        assertEquals("Player should have 3 GRAIN after adding 5 and then removing 2", 3, player.getResourceCount(Resource.GRAIN));

        //partition B: Removing all of a resource brings count to exactly 0
        player.addResource(Resource.BRICK, 4);
        player.removeResource(Resource.BRICK, 4);
        assertEquals("Player should have 0 BRICK after removing the full amount", 0, player.getResourceCount(Resource.BRICK));

        //partition C: boundary testing, must go to zero
        player.addResource(Resource.LUMBER, 1);
        player.removeResource(Resource.LUMBER, 1);
        assertEquals("Should be back to 0 after add(1) then remove(1) resources", 0, player.getResourceCount(Resource.LUMBER));
    }

    // =================================================================
    // GROUP 4: methods for record keeping
    // Partition: any valid integer ID should work without exception
    // cause the methods just track what player has built
    // road + settlement + city all tested together since they all just recordkeep, dont crash
    // =================================================================

    /**
     * TEST: no exception thrown
     * PURPOSE: calling road + settlement + city methods
     */
    @Test(timeout = TIMEOUT)
    public void test4_recordMethods() {
        assertDoesNotThrow(() -> {
            player.recordRoadBuilt(0);   // edge ID 0 is first possible edge
            player.recordRoadBuilt(10);  // edge ID somewhere in middle
            player.recordRoadBuilt(71);  // edge ID 71 is last edge on board
        });

        assertDoesNotThrow(() -> {
            player.recordSettlementBuilt(0);  // first node
            player.recordSettlementBuilt(53); // last node on Catan board
        });

        assertDoesNotThrow(() -> player.recordCityBuilt());
    }
}
