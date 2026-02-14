/**
 * The interface of dice is to make the code more coherent obeying rules of LSP.
 * The interface will be implemented later on in the multiDice and the regularDice classes.
 */
public interface Dice {
    /**
     * The roll method will be implemented in MultiDice and RegularDice.
     * @return Result of the roll as integer.
     */
    int roll();
}
