import java.util.*;
import java.io.IOException;

/**
 * Demonstrator code
 * Integrates regex parser, visualizer, and JSON export
 */
public class Demonstrator {

    private static final String VISUALIZER_JSON_PATH = "../visualizer/assignments/visualize/state.json";
    private static final String VISUALIZER_DIR = "../visualizer/assignments/visualize";

    public static void main(String[] args) {
        System.out.println("Settlers of Catan - Assignment 2 Demonstrator");
        System.out.println("\n");

        // Initialize game components
        System.out.println("Initializing game...");
        Board board = new Board();
        Map<Resource, Integer> resourceBank = new EnumMap<>(Resource.class);
        for (Resource r : Resource.values()) {
            resourceBank.put(r, 19);
        }
        Resources resources = new Resources(resourceBank);
        Random randomizer = new Random();
        Dice dice1 = new RegularDice(6, randomizer);
        Dice dice2 = new RegularDice(6, randomizer);
        MultiDice multiDice = new MultiDice();
        ResourceProduction production = new ResourceProduction(multiDice, resources, board);
        int maxRounds = 100;
        Game game = new Game(board, resources, production, maxRounds, randomizer);

        System.out.println("Game initialized successfully!");
        System.out.println("Players: ORANGE (YOU), WHITE (AI), RED (AI), BLUE (AI)\n");

        // Setup parser
        CommandParser parser = new CommandParser(board, resources, game.getOrangePlayer());
        Scanner scanner = new Scanner(System.in);

        System.out.println("Available commands:");
        System.out.println("  Roll                      - Roll dice and collect resources");
        System.out.println("  List                      - Show your current resources");
        System.out.println("  Build settlement [nodeId] - Build settlement at intersection");
        System.out.println("  Build city [nodeId]       - Upgrade settlement to city");
        System.out.println("  Build road [from], [to]   - Build road between intersections");
        System.out.println("  Go                        - End your turn\n");

        // Copy initial JSON to visualizer
        copyJsonToVisualizer();
        System.out.println("Initial game state exported to visualizer\n");

        // Launch visualizer
        System.out.println("Launching visualizer...");
        Process visualizerProcess = launchVisualizer();
        if (visualizerProcess != null) {
            System.out.println("Visualizer launched successfully!\n");
        }
        else {
            System.out.println("Could not auto-launch visualizer.");
            System.out.println("Please run manually: cd " + VISUALIZER_DIR);
            System.out.println("  source .venv/bin/activate");
            System.out.println("  python light_visualizer.py base_map.json --watch\n");
        }

        System.out.println("Waiting for visualizer to initialize...");
        try { Thread.sleep(3000); }
        catch (InterruptedException e) {}

        System.out.println("\nPress Enter to start the game");
        scanner.nextLine();

        System.out.println("\nStarting game...\n");

        // Main game loop
        runGameWithHumanPlayer(game, parser, scanner);

        // Cleanup
        scanner.close();
        if (visualizerProcess != null && visualizerProcess.isAlive()) {
            System.out.println("\nStopping visualizer...");
            visualizerProcess.destroy();
        }

        System.out.println("\nDemonstration complete!");
        System.out.println("Final game state saved to: " + VISUALIZER_JSON_PATH);
    }

    private static Process launchVisualizer() {
        try {
            String[] command = {
                    "/bin/bash",
                    "-c",
                    "cd " + VISUALIZER_DIR + " && source .venv/bin/activate && python light_visualizer.py base_map.json --watch"
            };
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            return process;
        }
        catch (IOException e) {
            System.err.println("Error launching visualizer: " + e.getMessage());
            return null;
        }
    }

    private static void copyJsonToVisualizer() {
        try {
            java.nio.file.Files.copy(
                    java.nio.file.Paths.get("gameState.json"),
                    java.nio.file.Paths.get(VISUALIZER_JSON_PATH),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        }
        catch (IOException e) {
            System.err.println("Warning: Could not copy JSON to visualizer: " + e.getMessage());
        }
    }

    private static void runGameWithHumanPlayer(Game game, CommandParser parser, Scanner scanner) {
        int round = 0;

        // Play for up to 20 rounds or until someone wins
        while (round < 20 && !game.checkWinner()) {
            round++;

            System.out.println("\n");
            System.out.println("ROUND " + round);
            System.out.println("\n");

            // Human turn
            System.out.println("YOUR TURN (ORANGE)");
            playHumanTurn(game, parser, scanner, round);

            if (game.checkWinner()) {
                break;
            }

            // AI turns
            System.out.println("\n AI TURNS");
            System.out.println("[" + round + "] / WHITE: AI turn");
            System.out.println("[" + round + "] / RED: AI turn");
            System.out.println("[" + round + "] / BLUE: AI turn");

            // Update JSON after AI turns
            game.writeJson("gameState.json");
            copyJsonToVisualizer();
            System.out.println("Game state updated after AI turns");

            // Step forward
            System.out.println("\nPress Enter to continue to next round");
            scanner.nextLine();
            System.out.println();
        }

        // Game end
        if (game.checkWinner()) {
            System.out.println("\nGAME OVER: We have a winner!");
            game.endGame();
        } else {
            System.out.println("\nDemo complete");
        }
    }

    private static void playHumanTurn(Game game, CommandParser parser, Scanner scanner, int round) {
        // Phase 1: Must roll first
        System.out.println("Type 'Roll' to roll the dice:");
        boolean hasRolled = false;

        while (!hasRolled) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            Command cmd = parser.parse(input);

            if (cmd instanceof Roll) {
                System.out.println("[" + round + "] / ORANGE: Rolled dice");
                System.out.println("Resources distributed to all players");

                game.writeJson("gameState.json");
                copyJsonToVisualizer();
                System.out.println("Board updated in visualizer");

                hasRolled = true;
                System.out.println("\nYou may now build or type 'Go' to end turn.");

            }
            else if (cmd instanceof Invalid) {
                Invalid inv = (Invalid) cmd;
                System.out.println("Invalid command: \"" + inv.getOriginalInput() + "\"");
                System.out.println("You must roll first. Type 'Roll'.");

            }
            else {
                System.out.println("You must roll before taking other actions!");
            }
        }

        // Phase 2: Build or pass
        boolean turnEnded = false;

        while (!turnEnded) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            Command cmd = parser.parse(input);

            if (cmd instanceof Go) {
                System.out.println("[" + round + "] / ORANGE: Turn ended");
                turnEnded = true;

            }
            else if (cmd instanceof ListCards) {
                System.out.println("Your resources:");
                Player orangePlayer = game.getOrangePlayer();
                for (Resource r : Resource.values()) {
                    int count = orangePlayer.getResourceCount(r);
                    System.out.println("  " + r + ": " + count);
                }

            }
            else if (cmd instanceof BuildSettlement) {
                BuildSettlement build = (BuildSettlement) cmd;
                int nodeId = build.getNodeId();

                System.out.println("[" + round + "] / ORANGE: Attempting to build settlement at node " + nodeId);

                Player orangePlayer = game.getOrangePlayer();
                Board board = game.getBoard();

                // Find the intersection by nodeId
                Intersection target = null;
                for (Intersection i : board.getIntersections()) {
                    if (i.getNodeID() == nodeId) {
                        target = i;
                        break;
                    }
                }

                boolean success = false;
                if (target != null) {
                    success = board.buildSettlement(orangePlayer, target);
                }

                if (success) {
                    System.out.println("Settlement built successfully!");
                    game.writeJson("gameState.json");
                    copyJsonToVisualizer();
                    System.out.println("Board updated in visualizer");
                }
                else {
                    System.out.println("Cannot build settlement there!");
                    if (target == null) {
                        System.out.println("(Invalid node ID)");
                    } else {
                        System.out.println("(Not enough resources, already occupied, or distance rule violated)");
                    }
                }

            }
            else if (cmd instanceof BuildCity) {
                BuildCity build = (BuildCity) cmd;
                int nodeId = build.getNodeId();

                System.out.println("[" + round + "] / ORANGE: Attempting to upgrade to city at node " + nodeId);

                Player orangePlayer = game.getOrangePlayer();
                Board board = game.getBoard();

                // Find the intersection
                Intersection target = null;
                for (Intersection i : board.getIntersections()) {
                    if (i.getNodeID() == nodeId) {
                        target = i;
                        break;
                    }
                }

                boolean success = false;
                if (target != null) {
                    success = board.buildCity(orangePlayer, target);
                }

                if (success) {
                    System.out.println("City built successfully!");
                    game.writeJson("gameState.json");
                    copyJsonToVisualizer();
                    System.out.println("Board updated in visualizer");
                }
                else {
                    System.out.println("Cannot build city there!");
                    if (target == null) {
                        System.out.println("(Invalid node ID)");
                    }
                    else {
                        System.out.println("(No settlement owned by you at that location, or not enough resources)");
                    }
                }

            }
            else if (cmd instanceof BuildRoad) {
                BuildRoad build = (BuildRoad) cmd;
                System.out.println("[" + round + "] / ORANGE: Road building disabled for demo (visualizer topology mismatch)");

            }
            else if (cmd instanceof Invalid) {
                Invalid inv = (Invalid) cmd;
                System.out.println("Invalid command: \"" + inv.getOriginalInput() + "\"");
                System.out.println("Try: Roll, Go, List, Build settlement [id], Build city [id]");
            }
        }
    }
}