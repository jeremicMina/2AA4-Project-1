package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//=========================
//CatanTestSuite.java
//run all test classes together as a suite
//=========================

@RunWith(Suite.class)
@Suite.SuiteClasses({
        //run in the order below
        PlayerTests.class,
        ResourcesTests.class,
        DiceTests.class,
        TileTests.class,
        BoardTests.class,
        ResourceProductionTests.class,
        CommandParserTest.class,
})

public class CatanTestSuite {}


























