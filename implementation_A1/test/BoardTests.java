import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

//=========================
// BoardTests.java
// Tests for the Board class (the game map and its building rules)
// Board is responsible for holding the 19 tiles, 54 intersections, 72 edges
// and enforcing all build rules for roads, settlements and cities
//=========================

public class BoardTests {

    private static final int TIMEOUT = 2000;
    private Board board;
    private Player p1;
    private Player p2;

    /*
     * use @Before to set up objects before each test.
     * To avoid passing the same object to next method and carrying over a possible failure
     * roads built in test A would still be there in test B otherwise
     */
    @Before
    public void setUp() {
        board = new Board();
        p1 = new Player(Color.ORANGE, 20);
        p2 = new Player(Color.RED, 21);
    }

    // =================================================================
    // GROUP 1: tests for board structure
    // =================================================================

    /**
     * TEST: Board has exactly 19 tiles, 54 intersections, 72 edges
     * verify the BoardConfig generated the correct structure
     */
    @Test(timeout = TIMEOUT)
    public void test15_board_structure() {
        assertEquals("Standard Catan board must have 19 tiles", 19, board.getTiles().size());
        assertEquals("Standard Catan board must have 54 intersections", 54, board.getIntersections().size());
        assertEquals("Standard Catan board must have 72 edges (slots for roads)", 72, board.getEdges().size());
    }

    // =================================================================
    // GROUP 2: tests for buildSettlement()
    //   P1: initial placement, intersection free, no adjacent owner -> true
    //   P2: intersection already has an owner -> false
    //   P3: adjacent intersection has an owner (distance rule) -> false
    //   P4: no road connecting player -> false
    // =================================================================

    /**
     * TEST: buildSettlement enforces all placement rules correctly
     * P1 + P2 + P3 + P4
     */
    @Test(timeout = TIMEOUT)
    public void test16_buildSettlement() {
        // P1: initial placement on a free node -> succeed
        Intersection target = board.getIntersections().get(0);
        boolean result = board.buildSettlement(p1, target, true);
        assertTrue("settlement on a free intersection must return true", result);
        assertEquals("Intersection owner should be p1 after building settlement", p1, target.getOwner());
        assertFalse("the new built settlement shouldn't be marked as a city", target.isCity());

        // P2: p2 tries to build on the same intersection already owned by p1 -> fail
        result = board.buildSettlement(p2, target, true);
        assertFalse("Building on an already occupied intersection must return false", result);
        assertEquals("Intersection should still belong to p1 (not p2)", p1, target.getOwner());

        // P3: try to build directly adjacent to p1's settlement -> fail cause of distance rule
        List<Intersection> neighbours = target.getAdjacentIntersections();
        assertFalse("Node 0 must have at least one neighbour on a valid board", neighbours.isEmpty());
        Intersection adjacent = neighbours.get(0);
        result = board.buildSettlement(p2, adjacent, true);
        assertFalse("Building adjacent to an existing settlement violates the distance rule", result);

        // P4: no road connecting player (when its not the initial building) -> fail
        // using the default overload (no third parameter) which sets isInitialPlacement=false
        Intersection farawayNode = board.getIntersections().get(5);
        result = board.buildSettlement(p1, farawayNode); // isInitialPlacement defaults to false
        assertFalse("Building a settlement without a connecting road must return false", result);
    }

    // =================================================================
    // GROUP 3: tests for buildRoad()
    //   P1: edge free, connected to player's settlement -> true
    //   P2: edge already owned -> false
    //   P3: edge free but not connected to player buildings -> false
    // =================================================================

    /**
     * TEST: buildRoad enforces all placement rules correctly
     * P1 + P2 + P3
     */
    @Test(timeout = TIMEOUT)
    public void test17_buildRoad() {
        //place p1 settlement to have a valid connection point
        Intersection node = board.getIntersections().get(0);
        board.buildSettlement(p1, node, true);

        // find any free edge touching that node
        Edge freeEdge = null;
        for (Edge e : node.getEdges()) { if (e.getOwner() == null) { freeEdge = e; break; } }
        assertNotNull("There must be at least one free edge touching node 0", freeEdge);

        // P1: connected to own settlement -> succeed
        boolean result = board.buildRoad(p1, freeEdge);
        assertTrue("buildRoad should return true when the edge connects to the player's settlement", result);
        assertEquals("The edge owner should be p1 after building the road", p1, freeEdge.getOwner());

        // P2: p2 tries to claim the same edge as p1 -> fail
        result = board.buildRoad(p2, freeEdge);
        assertFalse("buildRoad on an already-owned edge must return false", result);

        // P3: p1 has no settlements or roads near this edge -> fail
        Edge disconnected = board.getEdges().get(board.getEdges().size() - 1); // last edge far from p1
        // make sure this edge is not owned
        if (disconnected.getOwner() == null) {
            result = board.buildRoad(p2, disconnected); // p2 has no connection here
            assertFalse("buildRoad must return false when player has no connected settlement or road", result);
        }
    }

    // =================================================================
    // GROUP 4: tests for buildCity()
    //   P1: player owns a settlement there (can turn into city) -> true
    //   P2: a different player owns it -> false
    //   P3: node is already a city (a boundary to upgrade) -> false
    // =================================================================

    /**
     * TEST: buildCity enforces all upgrade rules for city
     */
    @Test(timeout = TIMEOUT)
    public void test18_buildCity() {
        Intersection node = board.getIntersections().get(0);
        board.buildSettlement(p1, node, true);

        // P1: upgrade player's own settlement to city -> succeed, mark node as city
        boolean result = board.buildCity(p1, node);
        assertTrue("buildCity should return true when player owns the settlement", result);
        assertTrue("Intersection should be marked as city after successful upgrade", node.isCity());

        // P2: p2 tries to upgrade p1's city -> fail
        // testing both wrong player and already a city rules together on same node
        result = board.buildCity(p2, node);
        assertFalse("buildCity must return false when a different player owns the intersection", result);

        // P3: p1 tries to upgrade the same node again -> should fail
        result = board.buildCity(p1, node);
        assertFalse("buildCity on a city already so intersection must return false", result);
    }
}
