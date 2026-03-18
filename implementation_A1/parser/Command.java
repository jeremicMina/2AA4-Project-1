package parser;

/**
 * command interface defines the contract for all player actions in the game loop
 * this interface follows the Command design pattern:
 *      - execute() performs the action
 *      - undo() reverses the action
 *      - name() identifies the command type (used for logging and dispatch)
 *
 * every class that implements Command must define all three methods.
 * Go, List, and Invalid have no state-changing effect so their undo() is empty.
 */

public interface Command {

    /**
     * returns the name/type of this command
     * used for logging and instanceof-free dispatch
     * @return string identifier of the command
     */
    String name();

    /**
     * executes the command
     * called by CommandHistory.execute() so the history stack stays in sync in case user desires to reverse the command
     */
    void execute();

    /**
     * reverses the effect of execute()
     * called by CommandHistory.undo() to walk the game state backwards
     * must leave the game in exactly the state it was before execute() was called
     */
    void undo(); //implemented to reverse actions
}
