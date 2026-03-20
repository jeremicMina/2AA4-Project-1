import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CommandParser {

    // context needed to create executable commands
    private Board board;
    private Resources resources;
    private Player player;

    /**
     * default constructor for cases where commands don't need game context
     * used by tests that only check parsing, not execution
     */
    public CommandParser() {
        this.board = null;
        this.resources = null;
        this.player = null;
    }

    /**
     * full constructor that injects game context so parsed commands
     * can execute and undo against real game state
     *
     * @param board game board
     * @param resources resource bank
     * @param player player whose turn it is
     */
    public CommandParser(Board board, Resources resources, Player player) {
        this.board = board;
        this.resources = resources;
        this.player = player;
    }

    // Regex patterns for each command type
    private static final Pattern ROLL_PATTERN =
            Pattern.compile("^Roll$", Pattern.CASE_INSENSITIVE);

    private static final Pattern GO_PATTERN =
            Pattern.compile("^Go$", Pattern.CASE_INSENSITIVE);

    private static final Pattern LIST_PATTERN =
            Pattern.compile("^List$", Pattern.CASE_INSENSITIVE);

    private static final Pattern BUILD_SETTLEMENT_PATTERN =
            Pattern.compile("^Build\\s+settlement\\s+(\\d+)$", Pattern.CASE_INSENSITIVE);

    private static final Pattern BUILD_CITY_PATTERN =
            Pattern.compile("^Build\\s+city\\s+(\\d+)$", Pattern.CASE_INSENSITIVE);

    private static final Pattern BUILD_ROAD_PATTERN =
            Pattern.compile("^Build\\s+road\\s+(\\d+)\\s*,\\s*(\\d+)$", Pattern.CASE_INSENSITIVE);

    public Command parse(String input) {
        // Handle null/empty input
        if (input == null || input.trim().isEmpty()) {
            return new Invalid(input);
        }

        input = input.trim();

        // Check ROLL
        if (ROLL_PATTERN.matcher(input).matches()) {
            return new Roll();
        }

        // Check GO
        if (GO_PATTERN.matcher(input).matches()) {
            return new Go();
        }

        // Check LIST
        if (LIST_PATTERN.matcher(input).matches()) {
            return new ListCards();
        }

        // Check BUILD SETTLEMENT
        Matcher settlementMatcher = BUILD_SETTLEMENT_PATTERN.matcher(input);
        if (settlementMatcher.matches()) {
            int nodeId = Integer.parseInt(settlementMatcher.group(1));
            return new BuildSettlement(nodeId, board, resources, player);
        }

        // Check BUILD CITY
        Matcher cityMatcher = BUILD_CITY_PATTERN.matcher(input);
        if (cityMatcher.matches()) {
            int nodeId = Integer.parseInt(cityMatcher.group(1));
            return new BuildCity(nodeId, board, resources, player);
        }

        // Check BUILD ROAD
        Matcher roadMatcher = BUILD_ROAD_PATTERN.matcher(input);
        if (roadMatcher.matches()) {
            int fromNode = Integer.parseInt(roadMatcher.group(1));
            int toNode = Integer.parseInt(roadMatcher.group(2));
            return new BuildRoad(fromNode, toNode, board, resources, player);
        }

        // If nothing matched, return InvalidCommand
        return new Invalid(input);
    }
}
