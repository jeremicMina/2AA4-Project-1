import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Player class used to instantiate the 4 players of the game, the identifier of the players
 * is color chosen from the color Enum, just like the Catan map implemented it.
 * We are tracking the records of the player within his class (encapsulation), such as the vpoints,
 * the list of settlements, cities and roads built by the player.
 */

public class Player {
    private Color color; // The identifier for the players will be the color just like it is mapped in the Catan map
    private int age;     // not needed now, but kept as per UML

    // Recording the number of roads, cities and settlements built per player
    private int roadsBuilt;
    private int citiesBuilt;
    private int settlementsBuilt;

    // Resource cards in hand of player
    // Note the use of Map (abstract) as declared type instead of concrete implementation (EnumMap)
    private Map<Resource, Integer> resources = new EnumMap<>(Resource.class);

    // Built piece locations (IDs)
    private List<Integer> roads = new ArrayList<>();        // edgeIDs
    private List<Integer> settlements = new ArrayList<>();  // nodeIDs

    /**
     * Player constructor used to instantiate players to the game through passing the following params
     * @param color The color is the id of the player, 4 colors => 4 players, using the Color Enum
     * @param age the integer indicating the age of the player
     */
    public Player(Color color, int age) {
        this.color = color;
        this.age = age;

        // initialize all resource counts to 0 at the start of the player state
        for (Resource r : Resource.values()) {
            resources.put(r, 0);
        }
    }

    // Getter method to return the player color identification
    public Color getColor() {
        return color;
    }

    // Getter method to return the resource count of the player of the resource passed as param
    public int getResourceCount(Resource r) {
        return resources.get(r);
    }

    // addResource is a method used to fill the bank of resources of the player by the quantity passed along
    void addResource(Resource r, int amount) {
        resources.put(r, getResourceCount(r) + amount);
    }

    // Inverse concept of addResource as now we are taking away from the player resources
    void removeResource(Resource r, int amount) {
        resources.put(r, getResourceCount(r) - amount);
    }

    // totalResourceCards is a method used to return the total value count of the resources
    int totalResourceCards() {
        int total = 0;
        for (Resource r : Resource.values()) {
            total += getResourceCount(r);
        }
        return total;
    }

    // Recording method used to track the number of roads built by the player
    void recordRoadBuilt(int edgeId) {
        roadsBuilt++;
        roads.add(edgeId);
    }

    // Recording method used to track the number of settlements built by the player
    void recordSettlementBuilt(int nodeId) {
        settlementsBuilt++;
        settlements.add(nodeId);
    }

    // Recording method used to track the number of cities built by the player
    void recordCityBuilt() {
        citiesBuilt++;
        // City replaces settlement: net +1 VP (settlement was 1, city is 2)
    }
}