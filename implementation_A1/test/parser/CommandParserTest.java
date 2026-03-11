package parser;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class CommandParserTest {

    private CommandParser parser;

    @Before
    public void setUp() {
        parser = new CommandParser();
    }

    @Test
    public void testParseRoll() {
        Command cmd = parser.parse("Roll");
        assertTrue("Should parse Roll command", cmd instanceof Roll);
    }

    @Test
    public void testParseGo() {
        Command cmd = parser.parse("Go");
        assertTrue("Should parse Go command", cmd instanceof Go);
    }

    @Test
    public void testParseList() {
        Command cmd = parser.parse("List");
        assertTrue("Should parse List command", cmd instanceof List);
    }

    @Test
    public void testParseBuildSettlement() {
        Command cmd = parser.parse("Build settlement 15");
        assertTrue("Should parse BuildSettlement", cmd instanceof BuildSettlement);
        assertEquals(15, ((BuildSettlement) cmd).getNodeId());
    }

    @Test
    public void testParseBuildCity() {
        Command cmd = parser.parse("Build city 10");
        assertTrue("Should parse BuildCity", cmd instanceof BuildCity);
        assertEquals(10, ((BuildCity) cmd).getNodeId());
    }

    @Test
    public void testParseBuildRoad() {
        Command cmd = parser.parse("Build road 5, 10");
        assertTrue("Should parse BuildRoad", cmd instanceof BuildRoad);
        BuildRoad road = (BuildRoad) cmd;
        assertEquals(5, road.getFromNodeId());
        assertEquals(10, road.getToNodeId());
    }

    @Test
    public void testCaseInsensitive() {
        Command cmd = parser.parse("ROLL");
        assertTrue("Should be case-insensitive", cmd instanceof Roll);
    }

    @Test
    public void testParseBuildRoadNoSpaces() {
        Command cmd = parser.parse("Build road 3,8");
        assertTrue("Should work without spaces", cmd instanceof BuildRoad);
    }

    @Test
    public void testParseInvalidCommand() {
        Command cmd = parser.parse("xyz");
        assertTrue("Should return Invalid", cmd instanceof Invalid);
    }

    @Test
    public void testParseNull() {
        Command cmd = parser.parse(null);
        assertTrue("Null should be invalid", cmd instanceof Invalid);
    }

    @Test
    public void testParseBuildSettlementWithWhitespace() {
        Command cmd = parser.parse("Build   settlement   25");
        assertTrue("Should handle extra whitespace", cmd instanceof BuildSettlement);
        assertEquals(25, ((BuildSettlement) cmd).getNodeId());
    }

    @Test
    public void testParseBuildRoadExtraSpaces() {
        Command cmd = parser.parse("Build road 12  ,  18");
        assertTrue("Should handle spaces around comma", cmd instanceof BuildRoad);
        BuildRoad road = (BuildRoad) cmd;
        assertEquals(12, road.getFromNodeId());
        assertEquals(18, road.getToNodeId());
    }

    @Test
    public void testInvalidCommandStoresOriginalInput() {
        Command cmd = parser.parse("random garbage");
        assertTrue("Should be Invalid", cmd instanceof Invalid);
        assertEquals("random garbage", ((Invalid) cmd).getOriginalInput());
    }

    @Test
    public void testParseBuildCityLowercase() {
        Command cmd = parser.parse("build city 42");
        assertTrue("Should be case-insensitive", cmd instanceof BuildCity);
        assertEquals(42, ((BuildCity) cmd).getNodeId());
    }

    @Test
    public void testParseLargeNodeId() {
        Command cmd = parser.parse("Build settlement 999");
        assertTrue("Should handle large node IDs", cmd instanceof BuildSettlement);
        assertEquals(999, ((BuildSettlement) cmd).getNodeId());
    }

    @Test
    public void testParseBuildRoadMixedCase() {
        Command cmd = parser.parse("BuILd RoAd 7, 14");
        assertTrue("Should handle mixed case", cmd instanceof BuildRoad);
        BuildRoad road = (BuildRoad) cmd;
        assertEquals(7, road.getFromNodeId());
        assertEquals(14, road.getToNodeId());
    }
}