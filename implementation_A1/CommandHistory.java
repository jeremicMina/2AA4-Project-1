import java.util.Stack;

/**
 * CommandHistory manages the undo and redo stacks for R3.1
 *
 * every human action in the game loop goes through execute() here instead of
 * being called directly, so the history is always in sync with what happened
 *
 * undo() walks the game state backwards one step at a time
 * redo() walks it forward again after an undo
 * doing a new action after an undo clears the redo stack
 * you can't redo something if you've already done something else after it
 */
public class CommandHistory {

    // stack of commands that was executed, top is most recent
    private final Stack<Command> undoStack = new Stack<>();

    // stack of commands that were undone, top is most recently undone
    private final Stack<Command> redoStack = new Stack<>();

    /**
     * executes a command and pushes it onto the undo stack
     * clears the redo stack because doing a new action after an undo
     * makes the previous redo history irrelevant
     *
     * @param c command to execute
     */
    public void execute(Command c) {
        c.execute();
        undoStack.push(c);
        // clear redo stack cause we can't redo after a new action
        redoStack.clear();
    }

    /**
     * undoes the most recent command
     * pops from the undo stack, then calls undo() on it, then pushes it
     * onto the redo stack so it can be redone if needed
     * does nothing if there is nothing to undo
     */
    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("nothing to undo.");
            return;
        }
        Command c = undoStack.pop();
        c.undo();
        redoStack.push(c);
    }

    /**
     * redoes the most recently undone command
     * pops from the redo stack, then calls execute() on it, then pushes it
     * back onto the undo stack so it can be undone again
     * does nothing if there is nothing to redo
     */
    public void redo() {
        if (redoStack.isEmpty()) {
            System.out.println("nothing to redo.");
            return;
        }
        Command c = redoStack.pop();
        c.execute();
        undoStack.push(c);
    }

    /**
     * @return true if there is a command to be undone
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * @return true if there is a command that can be redone
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * @return how many actions are currently on the undo stack
     */
    public int undoStackSize() {
        return undoStack.size();
    }
}