/**
 * Holds all rules and creates iterators, constraints first, then value rules
 */
public class RuleCollection {

    private Rule[] rules;

    /**
     * Creates the rule collection with all 6 rules in priority order
     * Constraints are checked first
     */
    public RuleCollection() {
        this.rules = new Rule[] {
                new MustSpendRule(),         // 10.0 - Must spend if >7 cards
                new ConnectRoadsRule(),      // 9.0 - Connect road segments
                new DefendLongestRoadRule(), // 8.0 - Defend longest road
                new EarnVPRule(),            // 1.0 - Earn victory points
                new BuildSomethingRule(),    // 0.8 - Build without earning VP
                new SpendToFiveRule()        // 0.5 - Reduce hand to <5 cards
        };
    }

    /**
     * Creates an iterator for iterating through the rules.
     */
    public RuleIterator createIterator(Player player, Board board, Resources resources) {
        return new RuleIterator(rules, player, board, resources);
    }

    /**
     * Returns the number of rules
     */
    public int size() {
        return rules.length;
    }
}