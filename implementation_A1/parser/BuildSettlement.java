package parser;

public class BuildSettlement implements Command{
    private int nodeId;

    public BuildSettlement(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() { return nodeId; }

    @Override
    public String name() { return "BUILD_SETTLEMENT"; }

    @Override
    public String toString() { return "BUILD_SETTLEMENT " + nodeId; }
}
