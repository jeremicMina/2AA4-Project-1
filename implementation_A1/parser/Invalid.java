package parser;

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
}
