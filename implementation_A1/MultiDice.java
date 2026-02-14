import java.util.ArrayList;
import java.util.List;
/**
 * MultiDice class implements the action of rolling many dices
 * It will implement the interface of Dice and will override
 * the roll method to achieve the MultiDice rolling function.
 * Class is final so we dont alter with its mechanism.
 */


public final class MultiDice implements Dice{
    /**
     * Creating a list of Dices passed by the user.
     */
    private List<Dice> multiDice = new ArrayList<>();

    /**
     * The addDice method is responsible for adding dice into the multiDice
     * @param dice the dice passed by the user
     */
    public void addDice(Dice dice){
        multiDice.add(dice);
    }

    /**
     * Rolling method to return a sum value of all dices rolled
     * @return Value of the rolling motion of all dices
     */
    @Override
    public int roll(){
        int sum = 0;
        for (Dice dice: multiDice){
            sum += dice.roll();
        }

        return sum;
    }
}
