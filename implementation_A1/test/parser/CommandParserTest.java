package test.parser;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;


public class CommandParserTest {

    private CommandParser parser;

    @Before
    public void setUp() {
        parser = new CommandParser();
    }

    // Test 1: Roll command
    @Test
    public void testParseRoll() {
        Command cmd = parser.parse("Roll");
        assertTrue("Should parse Roll command", cmd instanceof Roll);
    }

    // Test 2: Go command
    @Test
    public void testParseGo() {
        Command cmd = parser.parse("Go");
        assertTrue("Should parse Go command", cmd instanceof Go);
    }

    // Test 3: List command
    @Test
    public void testParseList() {
        Command cmd = parser.parse("List");
        assertTrue("Should parse List command", cmd instanceof List);
    }

    // Test 4: Build settlement
    @Test
    public void testParseBuildSettlement() {
        Command cmd = parser.parse("Build settlement 15");
        assertTrue("Should parse BuildSettlement", cmd instanceof BuildSettlement);
        assertEquals(15, ((BuildSettlement) cmd).getNodeId());
    }

    // Test 5: Build city
    @Test
    public void testParseBuildCity() {
        Command cmd = parser.parse("Build city 10");
        assertTrue("Should parse BuildCity", cmd instanceof BuildCity);
        assertEquals(10, ((BuildCity) cmd).getNodeId());
    }

    // Test 6: Build road
    @Test
    public void testParseBuildRoad() {
        Command cmd = parser.parse("Build road 5, 10");
        assertTrue("Should parse BuildRoad", cmd instanceof BuildRoad);
        BuildRoad road = (BuildRoad) cmd;
        assertEquals(5, road.getFromNodeId());
        assertEquals(10, road.getToNodeId());
    }

    // Test 7: Case insensitivity
    @Test
    public void testCaseInsensitive() {
        Command cmd = parser.parse("ROLL");
        assertTrue("Should be case-insensitive", cmd instanceof Roll);
    }

    // Test 8: Build road without spaces around comma
    @Test
    public void testParseBuildRoadNoSpaces() {
        Command cmd = parser.parse("Build road 3,8");
        assertTrue("Should work without spaces", cmd instanceof BuildRoad);
    }

    // Test 9: Invalid command
    @Test
    public void testParseInvalidCommand() {
        Command cmd = parser.parse("xyz");
        assertTrue("Should return Invalid", cmd instanceof Invalid);
    }

    // Test 10: Null/empty input
    @Test
    public void testParseNull() {
        Command cmd = parser.parse(null);
        assertTrue("Null should be invalid", cmd instanceof Invalid);
    }
}
