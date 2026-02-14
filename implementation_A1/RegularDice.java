import java.util.Random;
/**
 * RegularDice class implements the action of rolling one dice.
 * It will implement the interface of Dice and will override
 * the roll method to achieve the RegularDice rolling function.
 * Class is final so we dont alter with its mechanism.
 */


public final class RegularDice implements Dice{
    private final Random random; // Creating a random object to generate the roll motion
    private final int sides; //The implementation of the number of sides the dice has

    /**
     * Constructor for the RegularDice method to allow for different sides dices.
     * @param sides of the dice (generally we use 6), but we would like to keep it abstarct
     */
    public RegularDice(int sides, Random random){
        this.random = random;
        this.sides = sides;
    }

    /**
     * Rolling method to return a number value
     * @return Value of the rolling motion on the dice using the randomizer
     */
    @Override
    public int roll(){
        /**
         * Returning the number adding one as the bound inside the random is excluded,
         * so we add 1 to compensate for that.
         */
        return 1 + random.nextInt(sides);
    }
}
