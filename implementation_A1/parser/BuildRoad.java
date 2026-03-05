package parser;

public class BuildRoad implements Command{
    private int fromNodeId;
    private int toNodeId;

    public BuildRoad(int fromNodeId, int toNodeId) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
    }

    public int getFromNodeId() { return fromNodeId; }
    public int getToNodeId() { return toNodeId; }

    @Override
    public String name() { return "BUILD_ROAD"; }

    @Override
    public String toString() { return "BUILD_ROAD " + fromNodeId + " " + toNodeId; }
}
