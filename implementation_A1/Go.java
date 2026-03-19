/**
 * Go represents action of ending the current player's turn
 *
 * execute() and undo() are both empty because Go doesn't change any game state
 * it just signals the game loop to move to the next player
 */

public class Go implements Command{

    @Override
    public void execute() {}

    @Override
    public void undo() {}

    @Override
    public String name() {
        return "GO";
    }

    @Override
    public String toString() {
        return "GO";
    }
}
