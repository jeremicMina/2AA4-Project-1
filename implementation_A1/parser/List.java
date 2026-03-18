package parser;

/**
 * List represents action of listing current player's resources
 *
 * execute() and undo() both empty because List is only for read
 * it just prints resource counts and doesn't touch game state
 */

public class List implements Command{

    @Override
    public void execute() {}

    @Override
    public void undo() {}

    @Override
    public String name() {
        return "LIST";
    }

    @Override
    public String toString() {
        return "LIST";
    }
}
