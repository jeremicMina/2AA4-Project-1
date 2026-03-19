/**
 * ListCards represents action of listing current player's resources
 * Packages was renamed form List to ListCards to avoid conflict with List utility
 *
 * execute() and undo() both empty because List is only for read
 * it just prints resource counts and doesn't touch game state
 */

public class ListCards implements Command{

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
