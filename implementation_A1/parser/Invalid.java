package parser;

/**
 * invalid represents an unrecognized command input
 *
 * execute() and undo() are both empty because Invalid never reaches game since it's caught
 */

public class Invalid implements Command{

    private String ogInput;

    public Invalid(String ogInput) {
        this.ogInput = ogInput;
    }

    public String getOriginalInput() {
        return ogInput;
    }

    @Override
    public String name() {
        return "INVALID";
    }

    @Override
    public String toString() {
        return "INVALID: \"" + ogInput + "\"";
    }

    @Override
    public void execute() {}

    @Override
    public void undo() {}
}
