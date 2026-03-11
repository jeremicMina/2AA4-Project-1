import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

//=========================
// TileTests.java
// Tests for the Tile class and its getResource() helper
// Tile is final so nothing can extend it, only its methods are tested
//=========================
// =================================================================
// for Tile.getResource():
//   P1: Producing terrains (MOUNTAIN, FOREST, HILLS, FIELDS, PASTURE) -> each maps to a specific Resource
//   P2: Non-producing terrain (DESERT) -> maps to null (it produces nothing)
// =================================================================

public class TileTests {

    private static final int TIMEOUT = 2000;

    // =================================================================
    // GROUP 1: tests for Tile getters
    // did the constructor store all its fields correctly?
    // check tileID + terrain + token + empty intersections
    // =================================================================

    /**
     * TEST: Tile getters return exactly what was passed to the constructor
     * verify tileID, terrain and token come back correctly
     * verify getIntersections() returns empty list when none were added
     * null would crash any for-each loop in the production code
     */
    @Test(timeout = TIMEOUT)
    public void test12_tile_getters() {
        List<Intersection> emptyList = new ArrayList<>();
        Tile tile = new Tile(5, Terrain.FOREST, 6, emptyList);

        // verify each getter returns the value passed to constructor
        assertEquals("getTileID should return 5", 5, tile.getTileID());
        assertEquals("getTerrain should return FOREST", Terrain.FOREST, tile.getTerrain());
        assertEquals("getToken should return 6", 6, tile.getToken());

        // getIntersections must never be null even when nothing was added just be empty, test for boundary
        assertNotNull("getIntersections() must never return null", tile.getIntersections());
        assertTrue("getIntersections() should be empty when none were added", tile.getIntersections().isEmpty());
    }

    // =================================================================
    // GROUP 2: tests for getResource()
    // P1: terrain that produces a resource should return that resource
    // =================================================================

    /**
     * TEST: producing terrains
     * P1 -> all 5 producing terrains must map to their expected resource
     * if mapping is wrong, production would give out the wrong resource
     */
    @Test(timeout = TIMEOUT)
    public void test13_getResource_producingTerrains() {
        assertEquals("MOUNTAIN must produce ORE", Resource.ORE, Tile.getResource(Terrain.MOUNTAIN));
        assertEquals("FOREST must produce LUMBER", Resource.LUMBER, Tile.getResource(Terrain.FOREST));
        assertEquals("HILLS must produce BRICK", Resource.BRICK, Tile.getResource(Terrain.HILLS));
        assertEquals("FIELDS must produce GRAIN", Resource.GRAIN, Tile.getResource(Terrain.FIELDS));
        assertEquals("PASTURE must produce WOOL", Resource.WOOL, Tile.getResource(Terrain.PASTURE));
    }

    // =================================================================
    // GROUP 3: tests for getResource()
    // P2: desert has no production so produces null
    // =================================================================

    /**
     * TEST: DESER
     * P2 -> desert must return null
     */
    @Test(timeout = TIMEOUT)
    public void test14_getResource_desert_returnsNull() {
        assertNull("DESERT should return null because it produces no resource", Tile.getResource(Terrain.DESERT));
    }
}
