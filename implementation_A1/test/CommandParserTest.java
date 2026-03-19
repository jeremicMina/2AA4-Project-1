package parser;

import org.junit.*;
import static org.junit.Assert.*;

public class CommandParserTest {

    private static final int TIMEOUT = 2000;
    private CommandParser parser;

    /*
     * use @Before to set up objects before each test.
     * To avoid passing the same object to next method and carrying over a possible failure
     */
    @Before
    public void setUp() {
        parser = new CommandParser();
    }

    // GROUP 1: tests for simple commands
    // Partitions:
    // A) Roll command
    // B) Go command
    // C) List command
    // D) Case-insensitive parsing

    /**
     * TEST: simple commands parse to correct command types
     * PURPOSE: partitions A, B, C, D
     */
    @Test(timeout = TIMEOUT)
    public void test1_simpleCommands() {
        // Partition A: Roll command
        Command cmd = parser.parse("Roll");
        assertTrue("Should parse Roll command into a Roll object", cmd instanceof Roll);

        // Partition B: Go command
        parser = new CommandParser();
        cmd = parser.parse("Go");
        assertTrue("Should parse Go command into a Go object", cmd instanceof Go);

        // Partition C: List command
        parser = new CommandParser();
        cmd = parser.parse("List");
        assertTrue("Should parse List command into a List object", cmd instanceof List);

        // Partition D: case-insensitive command
        parser = new CommandParser();
        cmd = parser.parse("ROLL");
        assertTrue("Parser should be case-insensitive for Roll", cmd instanceof Roll);
    }

    // GROUP 2: tests for Build settlement
    // Partitions:
    // A) Normal settlement command
    // B) Settlement command with extra whitespace
    // C) Settlement command with large node ID

    /**
     * TEST: Build settlement command parses correctly
     * PURPOSE: partitions A, B, C
     */
    @Test(timeout = TIMEOUT)
    public void test2_buildSettlementParsing() {
        // Partition A: normal settlement command
        Command cmd = parser.parse("Build settlement 15");
        assertTrue("Should parse Build settlement 15 into BuildSettlement", cmd instanceof BuildSettlement);
        assertEquals("Node ID should be 15", 15, ((BuildSettlement) cmd).getNodeId());

        // Partition B: settlement command with extra whitespace
        parser = new CommandParser();
        cmd = parser.parse("Build   settlement   25");
        assertTrue("Should handle extra whitespace in Build settlement command", cmd instanceof BuildSettlement);
        assertEquals("Node ID should still be parsed correctly as 25", 25, ((BuildSettlement) cmd).getNodeId());

        // Partition C: large node ID
        parser = new CommandParser();
        cmd = parser.parse("Build settlement 999");
        assertTrue("Should handle large node IDs in Build settlement command", cmd instanceof BuildSettlement);
        assertEquals("Large node ID should be parsed correctly as 999", 999, ((BuildSettlement) cmd).getNodeId());
    }

    // GROUP 3: tests for Build city
    // Partitions:
    // A) Normal city command
    // B) Lowercase / case-insensitive city command

    /**
     * TEST: Build city command parses correctly
     * PURPOSE: partitions A, B
     */
    @Test(timeout = TIMEOUT)
    public void test3_buildCityParsing() {
        // Partition A: normal city command
        Command cmd = parser.parse("Build city 10");
        assertTrue("Should parse Build city 10 into BuildCity", cmd instanceof BuildCity);
        assertEquals("Node ID should be 10", 10, ((BuildCity) cmd).getNodeId());

        // Partition B: lowercase / case-insensitive city command
        parser = new CommandParser();
        cmd = parser.parse("build city 42");
        assertTrue("Build city command should be case-insensitive", cmd instanceof BuildCity);
        assertEquals("Lowercase Build city command should still parse node ID 42", 42, ((BuildCity) cmd).getNodeId());
    }

    // GROUP 4: tests for Build road
    // Partitions:
    // A) Normal road command
    // B) Road command without spaces after comma
    // C) Road command with extra spaces around comma
    // D) Mixed-case road command

    /**
     * TEST: Build road command parses correctly
     * PURPOSE: partitions A, B, C, D
     */
    @Test(timeout = TIMEOUT)
    public void test4_buildRoadParsing() {
        // Partition A: normal road command
        Command cmd = parser.parse("Build road 5, 10");
        assertTrue("Should parse Build road 5, 10 into BuildRoad", cmd instanceof BuildRoad);
        assertEquals("From-node should be 5", 5, ((BuildRoad) cmd).getFromNodeId());
        assertEquals("To-node should be 10", 10, ((BuildRoad) cmd).getToNodeId());

        // Partition B: no spaces after comma
        parser = new CommandParser();
        cmd = parser.parse("Build road 3,8");
        assertTrue("Should parse Build road command even without spaces after comma", cmd instanceof BuildRoad);
        assertEquals("From-node should be 3", 3, ((BuildRoad) cmd).getFromNodeId());
        assertEquals("To-node should be 8", 8, ((BuildRoad) cmd).getToNodeId());

        // Partition C: extra spaces around comma
        parser = new CommandParser();
        cmd = parser.parse("Build road 12  ,  18");
        assertTrue("Should parse Build road command with extra spaces around comma", cmd instanceof BuildRoad);
        assertEquals("From-node should be 12", 12, ((BuildRoad) cmd).getFromNodeId());
        assertEquals("To-node should be 18", 18, ((BuildRoad) cmd).getToNodeId());

        // Partition D: mixed-case command
        parser = new CommandParser();
        cmd = parser.parse("BuILd RoAd 7, 14");
        assertTrue("Build road command should be case-insensitive", cmd instanceof BuildRoad);
        assertEquals("From-node should be 7", 7, ((BuildRoad) cmd).getFromNodeId());
        assertEquals("To-node should be 14", 14, ((BuildRoad) cmd).getToNodeId());
    }

    // GROUP 5: tests for invalid input
    // Partitions:
    // A) Unknown command
    // B) Null input
    // C) Invalid input should store original text

    /**
     * TEST: invalid input returns Invalid object
     * PURPOSE: partitions A, B, C
     */
    @Test(timeout = TIMEOUT)
    public void test5_invalidInput() {
        // Partition A: unknown command
        Command cmd = parser.parse("xyz");
        assertTrue("Unknown command should return Invalid", cmd instanceof Invalid);

        // Partition B: null input
        parser = new CommandParser();
        cmd = parser.parse(null);
        assertTrue("Null input should return Invalid", cmd instanceof Invalid);

        // Partition C: invalid input stores original text
        parser = new CommandParser();
        cmd = parser.parse("random garbage");
        assertTrue("Random garbage input should return Invalid", cmd instanceof Invalid);
        assertEquals("Invalid command should store original input text", "random garbage", ((Invalid) cmd).getOriginalInput());
    }
}