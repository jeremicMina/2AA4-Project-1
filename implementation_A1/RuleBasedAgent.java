import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Uses the Iterator pattern to chose actions, picks the rule with the highest value
 */
public class RuleBasedAgent {

    private RuleCollection ruleCollection;
    private Random random;

    public RuleBasedAgent(Random random) {
        this.ruleCollection = new RuleCollection();
        this.random = random;
    }

    /**
     * Chooses the best action by iterating through all rules
     */
    public Command selectAction(Player player, Board board, Resources resources) {
        RuleIterator iterator = ruleCollection.createIterator(player, board, resources);

        double maxValue = -1.0;
        List<RuleIterator.RuleEvaluation> bestEvaluations = new ArrayList<>();

        // Iterate through all rules using the Iterator pattern
        while (iterator.hasNext()) {
            RuleIterator.RuleEvaluation eval = iterator.next();

            if (eval.value > maxValue) {
                // if better rule is found
                maxValue = eval.value;
                bestEvaluations.clear();
                bestEvaluations.add(eval);
            } else if (eval.value == maxValue && eval.value > 0.0) {
                // Tie adds to a list for random selection
                bestEvaluations.add(eval);
            }
        }

        // No valid rules found
        if (bestEvaluations.isEmpty() || maxValue == 0.0) {
            return null;
        }

        // Picks randomly if there's a tie
        RuleIterator.RuleEvaluation chosen =
                bestEvaluations.get(random.nextInt(bestEvaluations.size()));

        System.out.println("AI selected rule: " + chosen.ruleName +
                " (value: " + chosen.value + ")");

        return chosen.command;
    }
}