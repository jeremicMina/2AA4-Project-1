import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//=========================
//CatanTestSuite.java
//run all test classes together as a suite
//=========================

@RunWith(Suite.class)
@Suite.SuiteClasses({
        //run in the order below
        PlayerTests.class,             //file1: test 1-4
        ResourcesTests.class,          //file2: test 5-8
        DiceTests.class,               //file3: test 9-11
        TileTests.class,               //file4: test 12-14
        BoardTests.class,              //file5: test 15-18
        ResourceProductionTests.class, //file6: test 19-20
        CommandParserTest.class,       //file7: test
})

public class CatanTestSuite {}


























