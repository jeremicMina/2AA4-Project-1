package parser;

// do i need public final class?
public class Roll implements Command{
    @Override
    public String name() {
        return "ROLL";
    }

    @Override
    public String toString() {
        return "ROLL";
    }
}
