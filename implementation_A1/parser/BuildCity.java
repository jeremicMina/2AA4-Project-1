package parser;

public class BuildCity implements Command{
    private int nodeId;

    public BuildCity(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() { return nodeId; }

    @Override
    public String name() { return "BUILD_CITY"; }

    @Override
    public String toString() { return "BUILD_CITY " + nodeId; }
}
