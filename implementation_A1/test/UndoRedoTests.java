import org.junit.*;
import static org.junit.Assert.*;

public class UndoRedoTests {

    private static final int TIMEOUT = 2000;

    private Board board;
    private Resources resources;
    private Player player;
    private CommandHistory history;

    /*
     * fresh board, resources, player, and history before each test
     * so nothing carries over from a previous test that might have failed
     */
    @Before
    public void setUp() {
        board     = new Board();
        resources = Resources.createDefaultCollection();
        player    = new Player(Color.RED, 20);
        history   = new CommandHistory();
    }

    // --- helpers ---

    // gives the player exactly what a settlement costs: 1 BRICK, 1 LUMBER, 1 WOOL, 1 GRAIN
    private void giveSettlementResources() {
        resources.giveResources(1, player, Resource.BRICK);
        resources.giveResources(1, player, Resource.LUMBER);
        resources.giveResources(1, player, Resource.WOOL);
        resources.giveResources(1, player, Resource.GRAIN);
    }

    // gives the player exactly what a road costs: 1 BRICK, 1 LUMBER
    private void giveRoadResources() {
        resources.giveResources(1, player, Resource.BRICK);
        resources.giveResources(1, player, Resource.LUMBER);
    }

    // gives the player exactly what a city costs: 3 ORE, 2 GRAIN
    private void giveCityResources() {
        resources.giveResources(3, player, Resource.ORE);
        resources.giveResources(2, player, Resource.GRAIN);
    }

    // walks the board intersection list and returns the one matching the given id
    private Intersection intersectionById(int id) {
        for (Intersection i : board.getIntersections()) {
            if (i.getNodeID() == id) return i;
        }
        return null;
    }

    // places a settlement at nodeId using initial placement rules so no road is needed
    // does not spend resources, just sets up ownership state on the board
    private void placeInitialSettlement(int nodeId) {
        board.buildSettlement(player, intersectionById(nodeId), true);
    }

private int setupSettlementScenario() {
    placeInitialSettlement(0);
    giveRoadResources();
    int neighbourId = -1;
    for (Edge e : board.getEdges()) {
        int a = e.getIntersection1().getNodeID();
        int b = e.getIntersection2().getNodeID();
        if (a == 0 || b == 0) {
            board.buildRoad(player, e);
            neighbourId = (a == 0) ? b : a;
            break;
        }
    }
    giveSettlementResources();
    return neighbourId;
}

    // full setup for a BuildRoad test:
    // puts a settlement at node 0 so the edge is connected to a player piece,
    // then gives the player road resources
    // returns the first edge adjacent to node 0
    private Edge setupRoadScenario() {
        placeInitialSettlement(0);
        giveRoadResources();
        for (Edge e : board.getEdges()) {
            int a = e.getIntersection1().getNodeID();
            int b = e.getIntersection2().getNodeID();
            if (a == 0 || b == 0) return e;
        }
        throw new IllegalStateException("no edge adjacent to node 0 found");
    }

    // full setup for a BuildCity test:
    // sets the player as owner of node 0 directly and makes sure isCity is false,
    // then gives the player city resources
    private void setupCityScenario() {
        Intersection node0 = intersectionById(0);
        node0.setOwner(player);
        node0.setCity(false);
        giveCityResources();
    }

    // GROUP 1: tests for BuildSettlement execute()
    // Partitions:
    // A) valid node with road connection and enough resources
    // B) node id that does not exist on the board

    /**
     * TEST: execute on a reachable node sets the intersection owner to the player
     * PURPOSE: partition A
     */
    @Test(timeout = TIMEOUT)
    public void test1_buildSettlement_execute_setsOwner() {
        int target = setupSettlementScenario();
        history.execute(new BuildSettlement(target, board, resources, player));
        assertEquals("after execute, intersection owner should be the player",
                player, intersectionById(target).getOwner());
    }

    /**
     * TEST: execute on a node that does not exist does not spend any BRICK
     * PURPOSE: partition B
     */
    @Test(timeout = TIMEOUT)
    public void test2_buildSettlement_execute_invalidNode_noBrickSpent() {
        giveSettlementResources();
        new BuildSettlement(9999, board, resources, player).execute();
        assertEquals("no BRICK should be spent when the node does not exist on the board",
                1, player.getResourceCount(Resource.BRICK));
    }

    // GROUP 2: tests for BuildSettlement undo()
    // Partitions:
    // A) undo after a successful execute clears the owner
    // B) undo after a successful execute returns BRICK
    // C) undo after a successful execute returns LUMBER
    // D) undo after a successful execute returns WOOL
    // E) undo after a successful execute returns GRAIN
    // F) undo after a successful execute resets the isCity flag
    // G) undo when execute never succeeded does not crash or leak resources

    /**
     * TEST: undo clears the intersection owner back to null
     * PURPOSE: partition A
     */
    @Test(timeout = TIMEOUT)
    public void test3_buildSettlement_undo_clearsOwner() {
        int target = setupSettlementScenario();
        history.execute(new BuildSettlement(target, board, resources, player));
        history.undo();
        assertNull("after undo, intersection owner should be null",
                intersectionById(target).getOwner());
    }

    /**
     * TEST: undo gives the player their BRICK back
     * PURPOSE: partition B
     */
    @Test(timeout = TIMEOUT)
    public void test4_buildSettlement_undo_restoresBrick() {
        int target = setupSettlementScenario();
        int before = player.getResourceCount(Resource.BRICK);
        history.execute(new BuildSettlement(target, board, resources, player));
        history.undo();
        assertEquals("BRICK should be back to what it was before execute",
                before, player.getResourceCount(Resource.BRICK));
    }

    /**
     * TEST: undo gives the player their LUMBER back
     * PURPOSE: partition C
     */
    @Test(timeout = TIMEOUT)
    public void test5_buildSettlement_undo_restoresLumber() {
        int target = setupSettlementScenario();
        int before = player.getResourceCount(Resource.LUMBER);
        history.execute(new BuildSettlement(target, board, resources, player));
        history.undo();
        assertEquals("LUMBER should be back to what it was before execute",
                before, player.getResourceCount(Resource.LUMBER));
    }

    /**
     * TEST: undo gives the player their WOOL back
     * PURPOSE: partition D
     */
    @Test(timeout = TIMEOUT)
    public void test6_buildSettlement_undo_restoresWool() {
        int target = setupSettlementScenario();
        int before = player.getResourceCount(Resource.WOOL);
        history.execute(new BuildSettlement(target, board, resources, player));
        history.undo();
        assertEquals("WOOL should be back to what it was before execute",
                before, player.getResourceCount(Resource.WOOL));
    }

    /**
     * TEST: undo gives the player their GRAIN back
     * PURPOSE: partition E
     */
    @Test(timeout = TIMEOUT)
    public void test7_buildSettlement_undo_restoresGrain() {
        int target = setupSettlementScenario();
        int before = player.getResourceCount(Resource.GRAIN);
        history.execute(new BuildSettlement(target, board, resources, player));
        history.undo();
        assertEquals("GRAIN should be back to what it was before execute",
                before, player.getResourceCount(Resource.GRAIN));
    }

    /**
     * TEST: undo resets the isCity flag on the intersection to false
     * PURPOSE: partition F
     */
    @Test(timeout = TIMEOUT)
    public void test8_buildSettlement_undo_clearsCityFlag() {
        int target = setupSettlementScenario();
        history.execute(new BuildSettlement(target, board, resources, player));
        intersectionById(target).setCity(true); // force the flag on to make sure undo clears it
        history.undo();
        assertFalse("undo should always reset isCity to false on the intersection",
                intersectionById(target).isCity());
    }

    /**
     * TEST: undo after a failed execute does not crash and leaves the player with 0 BRICK
     * PURPOSE: partition G
     */
    @Test(timeout = TIMEOUT)
    public void test9_buildSettlement_undo_afterFailedExecute_noResourceLeak() {
        BuildSettlement cmd = new BuildSettlement(9999, board, resources, player);
        cmd.execute(); // does nothing since node 9999 does not exist
        cmd.undo();    // should also do nothing safely
        assertEquals("player should still have 0 BRICK after a failed execute and undo",
                0, player.getResourceCount(Resource.BRICK));
    }

    // GROUP 3: tests for BuildRoad execute()
    // Partitions:
    // A) valid edge connected to a player settlement
    // B) edge that does not exist on the board - no BRICK spent
    // C) edge that does not exist on the board - no LUMBER spent

    /**
     * TEST: execute on a valid connected edge sets the edge owner to the player
     * PURPOSE: partition A
     */
    @Test(timeout = TIMEOUT)
    public void test10_buildRoad_execute_setsEdgeOwner() {
        Edge target = setupRoadScenario();
        int from = target.getIntersection1().getNodeID();
        int to   = target.getIntersection2().getNodeID();
        history.execute(new BuildRoad(from, to, board, resources, player));
        assertEquals("after execute, the edge owner should be the player",
                player, target.getOwner());
    }

    /**
     * TEST: execute on an edge that does not exist does not spend BRICK
     * PURPOSE: partition B
     */
    @Test(timeout = TIMEOUT)
    public void test11_buildRoad_execute_invalidEdge_noBrickSpent() {
        giveRoadResources();
        new BuildRoad(999, 998, board, resources, player).execute();
        assertEquals("no BRICK should be spent when the edge does not exist",
                1, player.getResourceCount(Resource.BRICK));
    }

    /**
     * TEST: execute on an edge that does not exist does not spend LUMBER
     * PURPOSE: partition C
     */
    @Test(timeout = TIMEOUT)
    public void test12_buildRoad_execute_invalidEdge_noLumberSpent() {
        giveRoadResources();
        new BuildRoad(999, 998, board, resources, player).execute();
        assertEquals("no LUMBER should be spent when the edge does not exist",
                1, player.getResourceCount(Resource.LUMBER));
    }

    // GROUP 4: tests for BuildRoad undo()
    // Partitions:
    // A) undo after a successful execute clears the edge owner
    // B) undo after a successful execute returns BRICK
    // C) undo after a successful execute returns LUMBER
    // D) undo after a failed execute does not crash or leak resources

    /**
     * TEST: undo clears the edge owner back to null
     * PURPOSE: partition A
     */
    @Test(timeout = TIMEOUT)
    public void test13_buildRoad_undo_clearsEdgeOwner() {
        Edge target = setupRoadScenario();
        int from = target.getIntersection1().getNodeID();
        int to   = target.getIntersection2().getNodeID();
        history.execute(new BuildRoad(from, to, board, resources, player));
        history.undo();
        assertNull("after undo, the edge owner should be null",
                target.getOwner());
    }

    /**
     * TEST: undo gives the player their BRICK back
     * PURPOSE: partition B
     */
    @Test(timeout = TIMEOUT)
    public void test14_buildRoad_undo_restoresBrick() {
        Edge target = setupRoadScenario();
        int before = player.getResourceCount(Resource.BRICK);
        int from = target.getIntersection1().getNodeID();
        int to   = target.getIntersection2().getNodeID();
        history.execute(new BuildRoad(from, to, board, resources, player));
        history.undo();
        assertEquals("BRICK should be back to what it was before execute",
                before, player.getResourceCount(Resource.BRICK));
    }

    /**
     * TEST: undo gives the player their LUMBER back
     * PURPOSE: partition C
     */
    @Test(timeout = TIMEOUT)
    public void test15_buildRoad_undo_restoresLumber() {
        Edge target = setupRoadScenario();
        int before = player.getResourceCount(Resource.LUMBER);
        int from = target.getIntersection1().getNodeID();
        int to   = target.getIntersection2().getNodeID();
        history.execute(new BuildRoad(from, to, board, resources, player));
        history.undo();
        assertEquals("LUMBER should be back to what it was before execute",
                before, player.getResourceCount(Resource.LUMBER));
    }

    /**
     * TEST: undo after a failed execute does not crash and player has 0 BRICK
     * PURPOSE: partition D
     */
    @Test(timeout = TIMEOUT)
    public void test16_buildRoad_undo_afterFailedExecute_noResourceLeak() {
        BuildRoad cmd = new BuildRoad(999, 998, board, resources, player);
        cmd.execute(); // does nothing since the edge does not exist
        cmd.undo();    // should also do nothing safely
        assertEquals("player should still have 0 BRICK after a failed execute and undo",
                0, player.getResourceCount(Resource.BRICK));
    }

    // GROUP 5: tests for BuildCity execute()
    // Partitions:
    // A) player owns a settlement at the node and has enough resources
    // B) node is not owned by the player - isCity should stay false
    // C) node is not owned by the player - no ORE should be spent

    /**
     * TEST: execute upgrades the intersection to a city when the player owns it
     * PURPOSE: partition A
     */
    @Test(timeout = TIMEOUT)
    public void test17_buildCity_execute_setsIsCity() {
        setupCityScenario();
        history.execute(new BuildCity(0, board, resources, player));
        assertTrue("after execute, the intersection should be flagged as a city",
                intersectionById(0).isCity());
    }

    /**
     * TEST: execute on a node the player does not own does not set isCity
     * PURPOSE: partition B
     */
    @Test(timeout = TIMEOUT)
    public void test18_buildCity_execute_unownedNode_isCityStaysFalse() {
        giveCityResources();
        new BuildCity(0, board, resources, player).execute();
        assertFalse("an intersection the player does not own should not become a city",
                intersectionById(0).isCity());
    }

    /**
     * TEST: execute on a node the player does not own does not spend any ORE
     * PURPOSE: partition C
     */
    @Test(timeout = TIMEOUT)
    public void test19_buildCity_execute_unownedNode_noOreSpent() {
        giveCityResources();
        new BuildCity(0, board, resources, player).execute();
        assertEquals("no ORE should be spent when the build fails",
                3, player.getResourceCount(Resource.ORE));
    }

    // GROUP 6: tests for BuildCity undo()
    // Partitions:
    // A) undo after a successful execute downgrades the city back to a settlement
    // B) undo after a successful execute returns ORE
    // C) undo after a successful execute returns GRAIN
    // D) undo after a failed execute does not crash or leak resources

    /**
     * TEST: undo downgrades the city back, isCity becomes false
     * PURPOSE: partition A
     */
    @Test(timeout = TIMEOUT)
    public void test20_buildCity_undo_clearsIsCity() {
        setupCityScenario();
        history.execute(new BuildCity(0, board, resources, player));
        history.undo();
        assertFalse("after undo, the intersection should no longer be a city",
                intersectionById(0).isCity());
    }

    /**
     * TEST: undo gives the player their ORE back
     * PURPOSE: partition B
     */
    @Test(timeout = TIMEOUT)
    public void test21_buildCity_undo_restoresOre() {
        setupCityScenario();
        int before = player.getResourceCount(Resource.ORE);
        history.execute(new BuildCity(0, board, resources, player));
        history.undo();
        assertEquals("ORE should be back to what it was before execute",
                before, player.getResourceCount(Resource.ORE));
    }

    /**
     * TEST: undo gives the player their GRAIN back
     * PURPOSE: partition C
     */
    @Test(timeout = TIMEOUT)
    public void test22_buildCity_undo_restoresGrain() {
        setupCityScenario();
        int before = player.getResourceCount(Resource.GRAIN);
        history.execute(new BuildCity(0, board, resources, player));
        history.undo();
        assertEquals("GRAIN should be back to what it was before execute",
                before, player.getResourceCount(Resource.GRAIN));
    }

    /**
     * TEST: undo after a failed execute does not crash and player still has 0 ORE
     * PURPOSE: partition D
     */
    @Test(timeout = TIMEOUT)
    public void test23_buildCity_undo_afterFailedExecute_noException() {
        BuildCity cmd = new BuildCity(9999, board, resources, player);
        cmd.execute(); // does nothing since node 9999 does not exist
        cmd.undo();    // should also do nothing safely
        assertEquals("player should still have 0 ORE after a failed execute and undo",
                0, player.getResourceCount(Resource.ORE));
    }

    // GROUP 7: tests for CommandHistory canUndo() and canRedo() state
    // Partitions:
    // A) fresh history - canUndo false
    // B) fresh history - canRedo false
    // C) after one execute - canUndo true
    // D) after one execute - canRedo false
    // E) after execute then undo - canUndo false
    // F) after execute then undo - canRedo true
    // G) new execute after undo clears the redo stack
    // H) undoStackSize tracks the right number after executes and undos

    /**
     * TEST: a brand new history has nothing to undo
     * PURPOSE: partition A
     */
    @Test(timeout = TIMEOUT)
    public void test24_commandHistory_fresh_canUndoFalse() {
        assertFalse("a fresh history should have nothing to undo",
                history.canUndo());
    }

    /**
     * TEST: a brand new history has nothing to redo
     * PURPOSE: partition B
     */
    @Test(timeout = TIMEOUT)
    public void test25_commandHistory_fresh_canRedoFalse() {
        assertFalse("a fresh history should have nothing to redo",
                history.canRedo());
    }

    /**
     * TEST: after executing a command, canUndo should be true
     * PURPOSE: partition C
     */
    @Test(timeout = TIMEOUT)
    public void test26_commandHistory_afterExecute_canUndoTrue() {
        history.execute(new BuildSettlement(9999, board, resources, player));
        assertTrue("canUndo should be true after a command is executed",
                history.canUndo());
    }

    /**
     * TEST: after executing a command, canRedo should still be false
     * PURPOSE: partition D
     */
    @Test(timeout = TIMEOUT)
    public void test27_commandHistory_afterExecute_canRedoFalse() {
        history.execute(new BuildSettlement(9999, board, resources, player));
        assertFalse("canRedo should be false right after an execute with no prior undo",
                history.canRedo());
    }

    /**
     * TEST: after execute then undo, canUndo should be false
     * PURPOSE: partition E
     */
    @Test(timeout = TIMEOUT)
    public void test28_commandHistory_afterUndo_canUndoFalse() {
        history.execute(new BuildSettlement(9999, board, resources, player));
        history.undo();
        assertFalse("canUndo should be false once the only command has been undone",
                history.canUndo());
    }

    /**
     * TEST: after execute then undo, canRedo should be true
     * PURPOSE: partition F
     */
    @Test(timeout = TIMEOUT)
    public void test29_commandHistory_afterUndo_canRedoTrue() {
        history.execute(new BuildSettlement(9999, board, resources, player));
        history.undo();
        assertTrue("canRedo should be true after a command has been undone",
                history.canRedo());
    }

    /**
     * TEST: executing a new command after an undo wipes the redo stack
     * PURPOSE: partition G
     */
    @Test(timeout = TIMEOUT)
    public void test30_commandHistory_newExecuteAfterUndo_clearsRedoStack() {
        history.execute(new BuildSettlement(9999, board, resources, player));
        history.undo();
        history.execute(new BuildSettlement(9999, board, resources, player));
        assertFalse("doing a new action after an undo should clear canRedo",
                history.canRedo());
    }

    /**
     * TEST: undoStackSize correctly reflects two executes followed by one undo
     * PURPOSE: partition H
     */
    @Test(timeout = TIMEOUT)
    public void test31_commandHistory_undoStackSize_tracksCorrectly() {
        history.execute(new BuildSettlement(9999, board, resources, player));
        history.execute(new BuildSettlement(9999, board, resources, player));
        history.undo();
        assertEquals("stack size should be 1 after two executes and one undo",
                1, history.undoStackSize());
    }

    // GROUP 8: tests for undo and redo on empty stacks
    // Partitions:
    // A) undo on an empty stack does not crash
    // B) redo on an empty stack does not crash

    /**
     * TEST: calling undo when there is nothing to undo does not throw
     * PURPOSE: partition A
     */
    @Test(timeout = TIMEOUT)
    public void test32_commandHistory_undoOnEmptyStack_noException() {
        history.undo(); // nothing on the stack, should just print and return
        assertFalse("canUndo should still be false after undo on an empty stack",
                history.canUndo());
    }

    /**
     * TEST: calling redo when there is nothing to redo does not throw
     * PURPOSE: partition B
     */
    @Test(timeout = TIMEOUT)
    public void test33_commandHistory_redoOnEmptyStack_noException() {
        history.redo(); // nothing on the stack, should just print and return
        assertFalse("canRedo should still be false after redo on an empty stack",
                history.canRedo());
    }

    // GROUP 9: tests for redo after undo
    // Partitions:
    // A) redo re-executes the command and restores state
    // B) redo puts the command back on the undo stack so canUndo is true
    // C) redo consumes itself so canRedo is false after a single redo
    // D) multiple undos followed by multiple redos restore state in the right order

    /**
     * TEST: redo after undo brings the city flag back to true
     * PURPOSE: partition A
     */
    @Test(timeout = TIMEOUT)
    public void test34_commandHistory_redo_restoresState() {
        setupCityScenario();
        history.execute(new BuildCity(0, board, resources, player));
        history.undo();
        history.redo();
        assertTrue("redo should re-apply the city upgrade",
                intersectionById(0).isCity());
    }

    /**
     * TEST: after a redo, canUndo is true again
     * PURPOSE: partition B
     */
    @Test(timeout = TIMEOUT)
    public void test35_commandHistory_redo_canUndoTrue() {
        setupCityScenario();
        history.execute(new BuildCity(0, board, resources, player));
        history.undo();
        history.redo();
        assertTrue("canUndo should be true again after a redo",
                history.canUndo());
    }

    /**
     * TEST: after a redo, canRedo is false
     * PURPOSE: partition C
     */
    @Test(timeout = TIMEOUT)
    public void test36_commandHistory_redo_canRedoFalse() {
        setupCityScenario();
        history.execute(new BuildCity(0, board, resources, player));
        history.undo();
        history.redo();
        assertFalse("canRedo should be false once the redo stack is empty again",
                history.canRedo());
    }

    /**
     * TEST: two commands undone then redone both restore their intersections as cities
     * PURPOSE: partition D
     */
    @Test(timeout = TIMEOUT)
    public void test37_commandHistory_multipleUndoThenRedo_bothCitiesRestored() {
        Intersection node0 = intersectionById(0);
        Intersection node1 = intersectionById(1);
        node0.setOwner(player); node0.setCity(false);
        node1.setOwner(player); node1.setCity(false);
        giveCityResources();
        giveCityResources();

        history.execute(new BuildCity(0, board, resources, player));
        history.execute(new BuildCity(1, board, resources, player));
        history.undo();
        history.undo();
        history.redo();
        history.redo();

        // both cities should be back after redoing everything
        assertTrue("node 0 should be a city again after redo", node0.isCity());
        assertTrue("node 1 should be a city again after redo", node1.isCity());
    }
}
