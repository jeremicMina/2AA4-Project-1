/**
 * Rule interface for AI decision making, each rule evaluates the game state and returns a Command.
 */
public interface Rule {
    /**
     * Evaluates how beneficial the rule is
     * @return value score
     */
    double evaluate(Player player, Board board, Resources resources);

    /**
     * Creates the Command to execute this rule's action.
     * @return Command to execute
     */
    Command createCommand(Player player, Board board, Resources resources);

    /**
     * @return the name of the rule
     */
    String getName();
}