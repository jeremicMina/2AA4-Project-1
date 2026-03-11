import org.junit.*;
import static org.junit.Assert.*;
import java.util.Random;

//=========================
// DiceTests.java
// Tests for RegularDice.roll()
//      P1: roll a 6 sided dice resulting in [1...6] for a normal case
//      P2: roll a one sided dice to see if it always produces exactly 1 for boundary check
//      P3: give the dice the same seed and it must produce idetical sequence
// Tests for MultiDice.roll()
//      P1: test 2 dice -> sum between 1 to 12
//      P2: test 1 dice -> sum equals that die's roll
//      P3: test zero dice used for boundary testing -> sum has to be 0
//=========================

public class DiceTests {

    private static final int TIMEOUT = 2000;

    // =================================================================
    // GROUP 1: tests for RegularDice
    // P1 + P2 + P3 for RegularDice.roll()
    // =================================================================

    /**
     * TEST: RegularDice rolls always stay in valid range across all partitions
     * partition P1 -> 6-sided die rolled 1000 times must always give [1..6]
     * partition P2 -> 1-sided die must always return exactly 1 for boundary test
     * partition P3 -> two dice with the same seed must produce identical sequences
     */
    @Test(timeout = TIMEOUT)
    public void test9_regularDice() {
        // P1: 6-sided die, 1000 rolls -> every result must be in [1..6]
        // 1000 rolls gives statistical confidence the range is always respected
        RegularDice die = new RegularDice(6, new Random(0));
        for (int i = 0; i < 1000; i++) {
            int result = die.roll();
            assertTrue("RegularDice(6) roll #" + i + " was out of range [1..6]: " + result, result >= 1 && result <= 6);
        }

        // P2: boundary 1-sided die -> result must always be exactly 1
        RegularDice oneSidedDie = new RegularDice(1, new Random(99));
        for (int i = 0; i < 50; i++) {
            assertEquals("A 1-sided die must always return exactly 1", 1, oneSidedDie.roll());
        }

        // P3: two dice with the same seed -> must produce identical sequences
        RegularDice dieA = new RegularDice(6, new Random(42));
        RegularDice dieB = new RegularDice(6, new Random(42));
        for (int i = 0; i < 20; i++) {
            assertEquals("Dice with same seed should produce identical roll #" + i, dieA.roll(), dieB.roll());
        }
    }

    // =================================================================
    // GROUP 2: tests for MultiDice (valid cases)
    // P1 + P2 + P3 for MultiDice.roll()
    // =================================================================

    /**
     * TEST: MultiDice sums dice correctly when it contains dice
     * partition P1 -> two 6-sided dice should always sum to [2..12]
     * partition P2 -> MultiDice wrapping one d6 should behave exactly like that die [1..6];
     * ensure the wrapper doesnt add anything unexpectedly
     */
    @Test(timeout = TIMEOUT)
    public void test10_multiDice_withDice() {
        // P1: two d6, sum must always be in [2..12]
        MultiDice md = new MultiDice();
        md.addDice(new RegularDice(6, new Random(1)));
        md.addDice(new RegularDice(6, new Random(2)));
        for (int i = 0; i < 1000; i++) {
            int sum = md.roll();
            assertTrue("Two-d6 sum #" + i + " was outside [2..12]: " + sum, sum >= 2 && sum <= 12);
        }

        // P2: MultiDice wrapping exactly one d6 should give [1..6] (save behve as the d6 dice)
        MultiDice single = new MultiDice();
        single.addDice(new RegularDice(6, new Random(5)));
        for (int i = 0; i < 200; i++) {
            int result = single.roll();
            assertTrue("MultiDice wrapping one d6 should still give [1..6], got: " + result, result >= 1 && result <= 6);
        }
    }

    // =================================================================
    // GROUP 3: tests for MultiDice (boundary empty case)
    // P1 + P2 + P3 for MultiDice.roll()

    // Partition P3: zero dice -> boundary between no dice and one die
    // kept separate because it tests a completely different state of MultiDice
    // (empty vs having dice) and is a common source of NullPointerException bugs
    // =================================================================

    /**
     * TEST: MultiDice with no dice added returns 0
     * P3 -> boundary of 0, if MultiDice tries to iterate over an empty list and crashes, this catches it
     */
    @Test(timeout = TIMEOUT)
    public void test11_multiDice_boundary_noDice() {
        MultiDice md = new MultiDice(); // add NOTHING

        assertEquals("BOUNDARY: MultiDice with no dice should return 0 (sum of nothing)", 0, md.roll());
    }
}
