import java.util.EnumMap;
import java.util.Map;

/**
 * Resources class is a helper class that could be useful to handle the resources by either spending resources or
 * giving them, as well as checking the resource bank if still valid or not and creating a default setup for
 * resources for a map consisting of 19 tiles.
 */

public class Resources {
    /**
     * Declaring the Map as type Map and then specifying the actual type as Enummap,
     * as this is an implementation of the SOLID principles to lean more on abstraction.
     */
    private Map<Resource, Integer> resourceCounts = new EnumMap<>(Resource.class);

    /**
     * Constructor of resources copies the resource count from the given map.
     * @param initial initalizing the resources collection through a Map.
     */
    public Resources(Map<Resource, Integer> initial) {
        for (Resource r : Resource.values()) {
            resourceCounts.put(r, initial.get(r));
        }
    }

    /**
     * Creating the default Resources collection as per the Catan game having 19 tiles.
     */
    public static Resources createDefaultCollection() {
        /**
         * Declaring the Map as type Map and then specifying the actual type as Enummap,
         * as this is an implementation of the SOLID principles to lean more on abstraction.
         */
        Map<Resource, Integer> init = new EnumMap<>(Resource.class);
        for (Resource r : Resource.values()) {
            init.put(r, 19);
        }
        return new Resources(init);
    }

    /**
     * giveResources is a method that attributes the resources to a player passed
     * and checking teh validity of the operation through it
     * @return true if successful, false if bank insufficient.
     */
    public boolean giveResources(int numTaken, Player playerColor, Resource resource) {
        //Checking if the amount taken is valid (positive)
        if (numTaken <= 0) return false;
        // Checking if the amount desired to be taken is available to be taken or not
        int available = resourceCounts.get(resource);
        if (available < numTaken) {
            return false;
        }
        //Adding the resource to the player
        resourceCounts.put(resource, available - numTaken);
        playerColor.addResource(resource, numTaken);
        return true;
    }

    /**
     * The same logic used for giveResource but reversed as now the player is spending.
     *
     * @return true if successful, false if player insufficient.
     */
    public boolean spendResources(int numSpent, Player playerID, Resource resource) {
        //Checking if the amount spent is valid (positive)
        if (numSpent <= 0) return false;
        // Checking if the amount desired to be spent is inhand or not
        int inHand = playerID.getResourceCount(resource);
        if (inHand < numSpent) {
            return false;
        }
        //Removing the resource from the player collection
        playerID.removeResource(resource, numSpent);
        resourceCounts.put(resource, resourceCounts.get(resource) + numSpent);
        return true;
    }

    /**
     * Used by ResourceProduction to avoid partial distribution.
     * @param demand total requested resources for this production step
     * @return true if bank can provide ALL demanded resources
     */
    public boolean canProvideAll(Map<Resource, Integer> demand) {
        for (Map.Entry<Resource, Integer> e : demand.entrySet()) {
            Resource r = e.getKey();
            int needed = e.getValue();
            if (resourceCounts.getOrDefault(r, 0) < needed) {
                return false;
            }
        }
        return true;
    }
}