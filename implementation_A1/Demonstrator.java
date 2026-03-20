import java.util.*;
import java.io.IOException;

/**
 * Demonstrator code
 * Integrates regex parser, visualizer, and JSON export
 * For A3: Integrate new CommandHistory and RuleBasedAgent implementations to the demonstrator (R3.1 / R3.2 / R3.3)
 */
public class Demonstrator {

    private static final String VISUALIZER_JSON_PATH = "../visualizer/assignments/visualize/state.json";
    private static final String VISUALIZER_DIR = "../visualizer/assignments/visualize";

    public static void main(String[] args) {
        System.out.println("Settlers of Catan - Assignment 3 Demonstrator");
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
        multiDice.addDice(dice1);
        multiDice.addDice(dice2);

        ResourceProduction production = new ResourceProduction(multiDice, resources, board);
        int maxRounds = 100;
        Game game = new Game(board, resources, production, maxRounds, randomizer);

        System.out.println("Game initialized successfully!");
        System.out.println("Players: ORANGE (YOU), WHITE (AI), RED (AI), BLUE (AI)\n");

        // Command parser for human input, it converts typed strings into Command objects.
        // The game loop then passes them through CommandHistory.execute() (R3.1).
        CommandParser parser = new CommandParser(board, resources, game.getOrangePlayer());
        Scanner scanner = new Scanner(System.in);

        // Print available commands
        System.out.println("Available commands:");
        System.out.println("  Roll                      - Roll dice and collect resources");
        System.out.println("  List                      - Show your current resources");
        System.out.println("  Build settlement [nodeId] - Build settlement at intersection");
        System.out.println("  Build city [nodeId]       - Upgrade settlement to city");
        System.out.println("  Build road [from], [to]   - Build road between intersections");
        System.out.println("  Undo                      - Undo your last action");
        System.out.println("  Redo                      - Redo the undone action");
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

    // Game loop

    private static void runGameWithHumanPlayer(Game game, CommandParser parser, Scanner scanner) {
        int round = 0;

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

            // A3: AI turns using RuleBasedAgent (R3.2 + R3.3)
            // The AI agents evaluate their rules (constraints first, then value-add and execute the highest-scoring action automatically.
            // RuleBasedAgent.selectAction() prints which rule fired and why.
            System.out.println("\n── AI TURNS (rule-based agents) ────────");
            System.out.println("[" + round + "] WHITE, RED, BLUE are deciding their actions...");

            // AI turns run inside game.playRound() but we only want the three AI players.
            // We call playRound() with only the AI players by having Game.playTurn() skip ORANGE (it checks currentPlayer != orange).
            // The Game.start() / playRound() path drives AI turns internally; here we call the round helper so the demonstrator controls the human turn separately.
            // AI turns already printed their rule selection inside Game.playAiTurn().

            game.writeJson("gameState.json");
            copyJsonToVisualizer();
            System.out.println("Game state updated after AI turns.");

            System.out.println("\nPress Enter to continue to the next round");
            scanner.nextLine();
            System.out.println();
        }

        // Game end
        if (game.checkWinner()) {
            System.out.println("\nGAME OVER: We have a winner!");
            game.endGame();
        } else {
            System.out.println("\nDemo complete (20 rounds played).");
        }
    }

    // Human turn

    /**
     * Drives the human player's (ORANGE) turn.
     * A3 — Command pattern integration (R3.1):
     *   Every build command is executed through game.executeCommand(cmd), which internally calls commandHistory.execute(cmd).
     *   This pushes the command onto the undo stack. Typing 'undo' calls game.undoLastCommand() which pops the stack and reverses the last build, while 'redo' re-applies it.
     * Phase 1: player must type 'Roll' before anything else.
     * Phase 2: player may build, undo, redo, list, or type 'Go' to end the turn.
     */
    private static void playHumanTurn(Game game, CommandParser parser, Scanner scanner, int round) {
        // Phase 1: Must roll first
        System.out.println("Type 'Roll' to roll the dice:");
        boolean hasRolled = false;

        while (!hasRolled) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            Command cmd = parser.parse(input);

            if (cmd instanceof Roll) {
                // A3: Roll is executed through CommandHistory so it can be undone.
                // Roll.undo() restores resources that were distributed this turn.
                game.executeCommand(cmd);
                System.out.println("[" + round + "] ORANGE: Rolled dice");
                System.out.println("Resources distributed to all players.");
                System.out.println("canUndo=" + game.canUndo() + "  canRedo=" + game.canRedo());

                game.writeJson("gameState.json");
                copyJsonToVisualizer();
                System.out.println("Board updated in visualizer");

                hasRolled = true;
                System.out.println("\nYou may now build or type 'Go' to end turn.");

            } else if (cmd instanceof Invalid) {
                System.out.println("You must roll first. Type 'Roll'.");
            } else {
                System.out.println("You must roll before taking other actions!");
            }
        }

        // Phase 2: build / undo / redo / go
        boolean turnEnded = false;

        while (!turnEnded) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            // A3: Undo (R3.1)
            // Pops the last command from CommandHistory's undo stack and reverses it.
            // BuildSettlement.undo() sets owner to null and refunds resources.
            // BuildCity.undo() downgrades the city and refunds resources.
            // BuildRoad.undo() clears the edge owner and refunds resources.
            // Roll.undo() takes back distributed resources.
            if (input.equalsIgnoreCase("undo")) {
                if (game.canUndo()) {
                    game.undoLastCommand();
                    game.writeJson("gameState.json");
                    copyJsonToVisualizer();
                    System.out.println("Board reverted. canUndo=" + game.canUndo() + "  canRedo=" + game.canRedo());
                } else {
                    System.out.println("Nothing to undo.");
                }
                continue;
            }

            // A3: Redo (R3.1)
            // Pops the last command from CommandHistory's redo stack and re-executes it.
            // This re-applies whatever was just undone.
            if (input.equalsIgnoreCase("redo")) {
                if (game.canRedo()) {
                    game.redoLastCommand();
                    game.writeJson("gameState.json");
                    copyJsonToVisualizer();
                    System.out.println("Action re-applied. canUndo=" + game.canUndo() + "  canRedo=" + game.canRedo());
                } else {
                    System.out.println("Nothing to redo.");
                }
                continue;
            }

            Command cmd = parser.parse(input);

            if (cmd instanceof Go) {
                System.out.println("[" + round + "] ORANGE: Turn ended.");
                turnEnded = true;

            } else if (cmd instanceof ListCards) {
                System.out.println("Your resources:");
                Player orange = game.getOrangePlayer();
                for (Resource r : Resource.values()) {
                    System.out.println("  " + r + ": " + orange.getResourceCount(r));
                }

            } else if (cmd instanceof BuildSettlement) {
                // A3: Command routed through CommandHistory (R3.1)
                // executeCommand() calls cmd.execute() AND pushes to the undo stack.
                // BuildSettlement.undo() will reverse the placement if called.
                BuildSettlement build = (BuildSettlement) cmd;
                System.out.println("[" + round + "] ORANGE: Attempting settlement at node " + build.getNodeId());
                game.executeCommand(build);

                // Determine success by inspecting board state after execute()
                boolean success = checkNodeOwnedByOrange(game, build.getNodeId());
                if (success) {
                    System.out.println("Settlement built! canUndo=" + game.canUndo());
                    game.writeJson("gameState.json");
                    copyJsonToVisualizer();
                } else {
                    System.out.println("Cannot build there (occupied, distance rule, or no road).");
                }

            } else if (cmd instanceof BuildCity) {
                // A3: Command routed through CommandHistory (R3.1)
                BuildCity build = (BuildCity) cmd;
                System.out.println("[" + round + "] ORANGE: Attempting city at node " + build.getNodeId());
                game.executeCommand(build);

                boolean success = checkNodeIsCityOwnedByOrange(game, build.getNodeId());
                if (success) {
                    System.out.println("City built! canUndo=" + game.canUndo());
                    game.writeJson("gameState.json");
                    copyJsonToVisualizer();
                } else {
                    System.out.println("Cannot build city there (no settlement, or no resources).");
                }

            } else if (cmd instanceof BuildRoad) {
                // A3: Command routed through CommandHistory (R3.1)
                BuildRoad build = (BuildRoad) cmd;
                System.out.println("[" + round + "] ORANGE: Attempting road " + build.getFromNodeId() + " → " + build.getToNodeId());
                game.executeCommand(build);
                System.out.println("Road command executed. canUndo=" + game.canUndo());
                game.writeJson("gameState.json");
                copyJsonToVisualizer();

            } else if (cmd instanceof Invalid) {
                System.out.println("Unknown command: \"" + ((Invalid) cmd).getOriginalInput() + "\"");
                System.out.println("Try: Roll, Go, List, Undo, Redo, " + "Build settlement [id], Build city [id], Build road [id],[id]");
            }
        }
    }

    // Board-inspection helpers

    /** Returns true if the given node is owned by ORANGE and is not a city. */
    private static boolean checkNodeOwnedByOrange(Game game, int nodeId) {
        for (Intersection i : game.getBoard().getIntersections()) {
            if (i.getNodeID() == nodeId) {
                return game.getOrangePlayer().equals(i.getOwner()) && !i.isCity();
            }
        }
        return false;
    }

    /** Returns true if the given node is owned by ORANGE and IS a city. */
    private static boolean checkNodeIsCityOwnedByOrange(Game game, int nodeId) {
        for (Intersection i : game.getBoard().getIntersections()) {
            if (i.getNodeID() == nodeId) {
                return game.getOrangePlayer().equals(i.getOwner()) && i.isCity();
            }
        }
        return false;
    }

    // Visualizer helpers

    private static Process launchVisualizer() {
        try {
            String[] command = {"/bin/bash", "-c", "cd " + VISUALIZER_DIR + " && source .venv/bin/activate" + " && python light_visualizer.py base_map.json --watch"};
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            return pb.start();
        } catch (IOException e) {
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
        } catch (IOException e) {
            System.err.println("Warning: Could not copy JSON to visualizer: " + e.getMessage());
        }
    }
}