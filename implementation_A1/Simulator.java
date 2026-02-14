import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

/**
 * Simulator / main class is the class that is responsible for implementing a single simulation of the game
 * and alternates the turns as per the config class until max round reached and throws exceptions if failed
 * to alternate turns.
 */
public class Simulator {

    public static void main(String[] args) {
        // Read configuration file
        int maxRounds = 25; // default if config not provided
        if (args.length >= 1) {
            Integer parsed = Simulator.readTurnsFromConfig(args[0]);
            if (parsed != null) {
                maxRounds = parsed;
            }
        }

        // Create game components
        long seed = 42L; // deterministic demo runs for debugging/testing
        Random randomizer = new Random(seed);

        Board board = new Board(); // creates a valid map (tiles + nodes + edges)
        Resources bank = Resources.createDefaultCollection(); // finite bank (95 cards total in base game)

        // Dice redesign:
        // - RegularDice implements Dice
        // - MultiDice implements Dice and sums contained dice rolls
        Dice d1 = new RegularDice(6, randomizer);
        Dice d2 = new RegularDice(6, randomizer);
        MultiDice twoDice = new MultiDice();
        twoDice.addDice(d1);
        twoDice.addDice(d2);

        ResourceProduction production = new ResourceProduction(twoDice, bank, board);

        Game game = new Game(board, bank, production, maxRounds, randomizer);

        // Run the simulation
        game.start();
    }

    public static Integer readTurnsFromConfig(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // allow "turns: 123" (case-insensitive on key)
                if (line.toLowerCase(Locale.ROOT).startsWith("turns:")) {
                    String rhs = line.substring("turns:".length()).trim();
                    int value = Integer.parseInt(rhs);
                    if (value < 1 || value > 8192) {
                        throw new IllegalArgumentException("turns must be in [1..8192]. Found: " + value);
                    }
                    return value;
                }
            }
        } catch (IOException | NumberFormatException ex) {
            System.out.println("Config read failed (using default rounds). Reason: " + ex.getMessage());
        }
        return null;
    }
}