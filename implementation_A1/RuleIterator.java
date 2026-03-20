/**
 * Iterator pattern implementation for iterating through rules with hasNext() and next() methods.
 */
public class RuleIterator {

    private Rule[] rules;
    private int position = 0;

    private Player player;
    private Board board;
    private Resources resources;

    public RuleIterator(Rule[] rules, Player player, Board board, Resources resources) {
        this.rules = rules;
        this.player = player;
        this.board = board;
        this.resources = resources;
    }

    /**
     * Checks if there are more rules to evaluate
     */
    public boolean hasNext() {
        return position < rules.length;
    }

    /**
     * Returns the next rule evaluation and moves to the next position
     */
    public RuleEvaluation next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException();
        }

        Rule currentRule = rules[position++];

        // Evaluate the rule and create its command
        double value = currentRule.evaluate(player, board, resources);
        Command command = currentRule.createCommand(player, board, resources);

        return new RuleEvaluation(currentRule.getName(), value, command);
    }

    /**
     * Resets the iterator to the beginning
     */
    public void reset() {
        position = 0;
    }

    /**
     * A rule's evaluation result
     */
    public static class RuleEvaluation {
        public final String ruleName;
        public final double value;
        public final Command command;

        public RuleEvaluation(String ruleName, double value, Command command) {
            this.ruleName = ruleName;
            this.value = value;
            this.command = command;
        }
    }
}